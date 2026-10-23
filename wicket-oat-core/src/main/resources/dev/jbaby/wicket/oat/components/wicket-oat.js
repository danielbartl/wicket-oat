/*
 * Wicket Oat's own script, for the few components that need one: context menus
 * (ContextMenuBehavior), Escape in inline editors (OatEditableColumn), autocomplete
 * lists inside dialogs and popovers (OatAutoCompleteField, OatMultiSelectField),
 * Enter on a filter's chips (OatFilter), loading more list items on scroll
 * (OatLoadMoreList), form fields showing an error after an Ajax request (BaseOatField),
 * avatar pictures, loaded or not (OatAvatar), and dialogs (OatDialog).
 * It only reads data- attributes, so pages keep Wicket's strict, nonce-based CSP:
 * no inline scripts or event handlers.
 */
(function () {
    'use strict';
    if (window.WicketOat) {
        return;
    }
    window.WicketOat = {};

    function ajax(url, params) {
        new Wicket.Ajax.Call().ajax({ u: url, ep: params || {} });
    }

    // ---- Form fields: an error that appeared, changed or went away (BaseOatField) ----

    // Sets the text of the message rather than replacing it, so screen readers announce the
    // change in its role="status" region, and leaves the inputs, their focus and what the
    // user typed alone
    WicketOat.fieldFeedback = function (fieldId, feedbackId, text, invalid) {
        var field = document.getElementById(fieldId);
        var feedback = document.getElementById(feedbackId);
        if (!field || !feedback) {
            return;
        }
        feedback.textContent = text;
        var container = field.querySelector('[data-field]');
        if (container) {
            container.setAttribute('data-field', invalid ? 'error' : '');
        }
        field.querySelectorAll('[aria-describedby~="' + feedbackId + '"]').forEach(function (input) {
            input.setAttribute('aria-invalid', invalid ? 'true' : 'false');
        });
    };

    // ---- Context menus: [data-oat-context-menu] holds the entries as JSON ----

    var menu = null;
    var opener = null;

    function ensureMenu() {
        if (menu) {
            return menu;
        }
        menu = document.createElement('menu');
        // Manual, not auto: on Linux and macOS the contextmenu event comes on mouse-down,
        // and auto's light dismiss would close the menu again on mouse-up
        menu.setAttribute('popover', 'manual');
        menu.setAttribute('role', 'menu');
        menu.className = 'oat-context-menu';
        menu.addEventListener('keydown', onMenuKey);
        document.body.appendChild(menu);

        // Dismissed by a press outside it, Escape, scrolling or leaving the window
        document.addEventListener('pointerdown', function (event) {
            if (!menu.contains(event.target)) {
                closeMenu();
            }
        }, true);
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && isOpen()) {
                event.preventDefault();
                closeMenu();
            }
        });
        window.addEventListener('scroll', function (event) {
            if (!menu.contains(event.target)) {
                closeMenu();
            }
        }, true);
        window.addEventListener('resize', closeMenu);
        window.addEventListener('blur', closeMenu);
        return menu;
    }

    function isOpen() {
        return menu.matches(':popover-open');
    }

    function closeMenu() {
        if (menu && isOpen()) {
            var hadFocus = menu.contains(document.activeElement);
            menu.hidePopover();
            // Give focus back to what the menu was opened on
            if (hadFocus && opener && opener.isConnected) {
                opener.focus({ preventScroll: true });
            }
        }
    }

    function items() {
        return Array.prototype.slice.call(menu.querySelectorAll('[role="menuitem"]'));
    }

    function focusItem(index) {
        var all = items();
        if (all.length) {
            all[(index + all.length) % all.length].focus();
        }
    }

    function onMenuKey(event) {
        var all = items();
        var current = all.indexOf(document.activeElement);
        if (event.key === 'ArrowDown') {
            focusItem(current + 1);
        } else if (event.key === 'ArrowUp') {
            focusItem(current - 1);
        } else if (event.key === 'Home') {
            focusItem(0);
        } else if (event.key === 'End') {
            focusItem(all.length - 1);
        } else if (event.key === 'Tab') {
            closeMenu();
            return;
        } else {
            return;
        }
        event.preventDefault();
    }

    function openMenu(target, x, y) {
        var entries;
        try {
            entries = JSON.parse(target.getAttribute('data-oat-context-menu'));
        } catch (e) {
            return;
        }
        ensureMenu();
        closeMenu();
        // Into the dialog or popover the element is in, as for autocomplete lists below:
        // outside an open modal dialog or drawer the menu would be inert, and outside a
        // popover a click on it would close the popover
        var layer = target.closest('dialog, [popover]') || document.body;
        if (menu.parentNode !== layer) {
            layer.appendChild(menu);
        }
        menu.replaceChildren();
        opener = target;
        entries.forEach(function (entry, index) {
            var button = document.createElement('button');
            button.type = 'button';
            button.className = 'ghost';
            button.tabIndex = -1;
            button.setAttribute('role', 'menuitem');
            if (entry.variant) {
                button.setAttribute('data-variant', entry.variant);
            }
            button.textContent = entry.label;
            button.addEventListener('click', function () {
                closeMenu();
                ajax(target.getAttribute('data-oat-context-url'), { action: String(index) });
            });
            menu.appendChild(button);
        });

        // Show it first to measure it, then keep it inside the window
        menu.style.left = '0px';
        menu.style.top = '0px';
        menu.showPopover();
        var size = menu.getBoundingClientRect();
        menu.style.left = Math.max(4, Math.min(x, window.innerWidth - size.width - 4)) + 'px';
        menu.style.top = Math.max(4, Math.min(y, window.innerHeight - size.height - 4)) + 'px';
        focusItem(0);
    }

    document.addEventListener('contextmenu', function (event) {
        var target = event.target.closest ? event.target.closest('[data-oat-context-menu]') : null;
        if (!target) {
            return;
        }
        event.preventDefault();
        var x = event.clientX;
        var y = event.clientY;
        if (!x && !y) {
            // Opened from the keyboard (Shift+F10, the Menu key): below the element
            var box = target.getBoundingClientRect();
            x = box.left;
            y = box.bottom;
        }
        openMenu(target, x, y);
    });

    // ---- Escape in a [data-oat-escape="cancel"] form clicks its [data-oat-cancel] button ----

    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Escape' || !event.target.closest) {
            return;
        }
        var form = event.target.closest('[data-oat-escape="cancel"]');
        var cancel = form && form.querySelector('[data-oat-cancel]');
        if (cancel) {
            event.preventDefault();
            cancel.click();
        }
    });

    // ---- Enter on a chip of an OatFilter (.oat-filter) chooses nothing ----

    // Some browsers submit a form when Enter is pressed on a radio button: a full page
    // request that skips the filter's Ajax update
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Enter' && event.target.matches && event.target.matches('.oat-filter input')) {
            event.preventDefault();
        }
    });

    // ---- Autocomplete lists in dialogs and popovers ----
    //
    // Wicket adds an autocomplete field's suggestion list (.wicket-aa-container) to
    // <body>. A modal <dialog> or an open popover sits in the browser's top layer, above
    // everything in the page, so the list would open behind it. For a field inside one,
    // move the list into it (so clicking the list counts as inside and doesn't close it)
    // and show the list as a manual popover, in the top layer above it. A top-layer
    // element is positioned against the page, so Wicket's own coordinates still apply.

    function layerOf(container) {
        var input = document.getElementById(container.id.replace(/-autocomplete-container$/, ''));
        return input && input.closest ? input.closest('dialog, [popover]') : null;
    }

    function syncAutocomplete(container) {
        var layer = layerOf(container);
        if (!layer || !container.showPopover) {
            return;
        }
        if (container.parentNode !== layer) {
            layer.appendChild(container);
        }
        if (!container.hasAttribute('popover')) {
            container.setAttribute('popover', 'manual');
        }
        var shown = container.style.display !== 'none' && !container.hasAttribute('hidden');
        var open = container.matches(':popover-open');
        if (shown && !open && layer.isConnected) {
            container.showPopover();
        } else if (!shown && open) {
            container.hidePopover();
        }
    }

    // Escape with the list open closes the list only (Wicket hides it), not the dialog
    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Escape' || !event.target || !event.target.id) {
            return;
        }
        var list = document.getElementById(event.target.id + '-autocomplete-container');
        if (list && list.hasAttribute('popover') && list.matches(':popover-open')) {
            event.preventDefault();
        }
    }, true);

    if ('MutationObserver' in window) {
        var visibility = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                syncAutocomplete(mutation.target);
            });
        });
        var watch = function (node) {
            if (node.nodeType === 1 && node.classList.contains('wicket-aa-container') && !node.oatWatched) {
                node.oatWatched = true;
                visibility.observe(node, { attributes: true, attributeFilter: ['style', 'hidden'] });
                syncAutocomplete(node);
            }
        };
        var startWatching = function () {
            new MutationObserver(function (mutations) {
                mutations.forEach(function (mutation) {
                    mutation.addedNodes.forEach(watch);
                });
            }).observe(document.body, { childList: true });
            document.querySelectorAll('.wicket-aa-container').forEach(watch);
        };
        if (document.body) {
            startWatching();
        } else {
            document.addEventListener('DOMContentLoaded', startWatching);
        }
    }

    // ---- Dialogs (OatDialog) ----

    // Shows a dialog rendered while open. One that was open already (data-oat-instant) shows
    // at once, without sliding or fading in again; after that, closing it animates.
    WicketOat.showDialog = function (id) {
        var dialog = document.getElementById(id);
        if (!dialog) {
            return;
        }
        if (!dialog.open) {
            dialog.showModal();
        }
        requestAnimationFrame(function () {
            requestAnimationFrame(function () {
                dialog.removeAttribute('data-oat-instant');
            });
        });
    };

    var FOCUSABLE = 'input:not([type=hidden]), select, textarea, button, a[href], [tabindex]';

    // Opening focuses the first field, not the "×" that comes before it
    document.addEventListener('toggle', function (event) {
        var dialog = event.target;
        if (event.newState !== 'open' || !(dialog instanceof HTMLDialogElement)) {
            return;
        }
        var close = dialog.querySelector('.oat-dialog-close');
        if (!close || document.activeElement !== close) {
            return;
        }
        var field = Array.prototype.find.call(dialog.querySelectorAll(FOCUSABLE), function (element) {
            return element !== close && !element.disabled && element.tabIndex >= 0 && element.getClientRects().length > 0;
        });
        if (field) {
            field.focus();
        }
    }, true);

    // Without Cancel and Confirm (setFooterVisible(false)), Enter in a field submits nothing,
    // where the browser would submit the dialog's form, or a page's form around it
    document.addEventListener('keydown', function (event) {
        var input = event.target;
        if (event.key !== 'Enter' || !input.matches
            || !input.matches('input:not([type=submit], [type=button], [type=reset], [type=image])')) {
            return;
        }
        var form = input.closest('.oat-dialog-form');
        if (form && !form.querySelector(':scope > footer')) {
            event.preventDefault();
        }
    });

    // ---- Load more on scroll: clicks [data-oat-load-on-scroll] when it comes into view ----

    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting && !entry.target.hasAttribute('aria-busy')) {
                    entry.target.click();
                }
            });
        }, { rootMargin: '200px' });

        var observe = function () {
            document.querySelectorAll('[data-oat-load-on-scroll]').forEach(function (button) {
                if (!button.oatObserved) {
                    button.oatObserved = true;
                    observer.observe(button);
                }
            });
        };
        // Buttons re-rendered over Ajax are new elements. Wicket's Ajax script can come after
        // this one in <head>, e.g. when an Oat field above the list added this one first, so
        // subscribe once the page has loaded
        var start = function () {
            observe();
            if (window.Wicket && Wicket.Event) {
                Wicket.Event.subscribe('/dom/node/added', observe);
            }
        };
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', start);
        } else {
            start();
        }
    }

    // ---- Avatars: a picture covers the initials under it once it has loaded, and one
    // that can't be loaded gives way to them ----

    var AVATAR_PICTURE = 'figure[data-variant="avatar"] > img';

    function pictureLoaded(img) {
        // Opaque from now on (wicket-oat.css)
        img.setAttribute('data-loaded', '');
    }

    // Load and error events don't bubble, but they pass the document on their way to the image
    document.addEventListener('load', function (event) {
        if (event.target.matches && event.target.matches(AVATAR_PICTURE)) {
            pictureLoaded(event.target);
        }
    }, true);
    document.addEventListener('error', function (event) {
        if (event.target.matches && event.target.matches(AVATAR_PICTURE)) {
            event.target.hidden = true;
        }
    }, true);

    // Pictures that loaded or failed before this script ran
    var checkPictures = function () {
        document.querySelectorAll(AVATAR_PICTURE).forEach(function (img) {
            if (!img.complete) {
                return;
            }
            if (img.naturalWidth > 0) {
                pictureLoaded(img);
            } else {
                // Failed, or an SVG without a size of its own: only decoding tells
                img.decode().then(function () {
                    pictureLoaded(img);
                }, function () {
                    img.hidden = true;
                });
            }
        });
    };
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', checkPictures);
    } else {
        checkPictures();
    }
})();
