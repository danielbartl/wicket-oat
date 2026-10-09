/*
 * Wicket Oat's own script, for the few components that need one: context menus
 * (ContextMenuBehavior) and loading more list items on scroll (OatLoadMoreList).
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
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', observe);
        } else {
            observe();
        }
        // Buttons re-rendered over Ajax are new elements
        if (window.Wicket && Wicket.Event) {
            Wicket.Event.subscribe('/dom/node/added', observe);
        }
    }
})();
