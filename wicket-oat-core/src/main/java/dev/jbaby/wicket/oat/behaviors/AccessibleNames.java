package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.markup.parser.IXmlPullParser.HttpTagType;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import java.util.Set;

/**
 * Whether rendered markup gives an element an accessible name, the text a screen reader
 * announces for it: a lenient form of the W3C's accessible name computation, for the
 * development-mode checks ({@link dev.jbaby.wicket.oat.OatSettings#setAccessibilityChecks}).
 */
final class AccessibleNames {

    // Elements without a body, which have no close tag in HTML
    private static final Set<String> VOID = Set.of("area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "source", "track", "wbr");

    private AccessibleNames() {
    }

    /**
     * Whether the element {@code html} starts with has a name: an {@code aria-label},
     * {@code aria-labelledby} or {@code title} of its own, or inside it text, a labelled
     * element or an image with alt text, outside anything hidden from screen readers. An
     * {@code <input>} always has one (its value, or the browser's "Submit").
     */
    static boolean hasName(CharSequence html) {
        XmlPullParser parser = new XmlPullParser();
        try {
            parser.parse(html);
            String root = null;
            int depth = 0;
            String hidden = null;
            int hiddenDepth = 0;
            for (HttpTagType type = next(parser); type != HttpTagType.NOT_INITIALIZED; type = next(parser)) {
                if (type == HttpTagType.BODY) {
                    if (root != null && hidden == null && hasText(parser.getString())) {
                        return true;
                    }
                    continue;
                }
                if (type != HttpTagType.TAG) {
                    continue;
                }
                XmlTag tag = parser.getElement();
                String name = name(tag);
                if (root == null) {
                    if (tag.isClose()) {
                        continue;
                    }
                    if (named(tag) || name.equals("input")) {
                        return true;
                    }
                    if (tag.isOpenClose() || VOID.contains(name)) {
                        return false;
                    }
                    root = name;
                    depth = 1;
                    continue;
                }
                if (name.equals(root)) {
                    depth += nesting(tag);
                    if (depth == 0) {
                        return false;
                    }
                }
                if (hidden != null) {
                    if (name.equals(hidden)) {
                        hiddenDepth += nesting(tag);
                        if (hiddenDepth == 0) {
                            hidden = null;
                        }
                    }
                } else if (!tag.isClose()) {
                    if (isHidden(tag)) {
                        if (tag.isOpen() && !VOID.contains(name)) {
                            hidden = name;
                            hiddenDepth = 1;
                        }
                    } else if (named(tag) || name.equals("img") && hasText(tag.getAttribute("alt"))) {
                        return true;
                    }
                }
            }
            // Nothing rendered: nothing to name
            return root == null;
        } catch (IOException | ParseException e) {
            // Markup we can't read isn't ours to judge
            return true;
        }
    }

    // The next element; for text, without the tag before it (the parser returns text from
    // the position marker on)
    private static HttpTagType next(XmlPullParser parser) throws ParseException {
        parser.setPositionMarker();
        return parser.next();
    }

    private static String name(XmlTag tag) {
        String name = tag.getNamespace() != null ? tag.getNamespace() + ":" + tag.getName() : tag.getName();
        return name.toLowerCase(Locale.ROOT);
    }

    private static int nesting(XmlTag tag) {
        return tag.isOpen() ? 1 : tag.isClose() ? -1 : 0;
    }

    private static boolean named(XmlTag tag) {
        return hasText(tag.getAttribute("aria-label")) || hasText(tag.getAttribute("aria-labelledby"))
                || hasText(tag.getAttribute("title"));
    }

    private static boolean isHidden(XmlTag tag) {
        return "true".equals(String.valueOf(tag.getAttribute("aria-hidden"))) || tag.getAttributes().containsKey("hidden");
    }

    private static boolean hasText(CharSequence text) {
        return text != null && !text.toString()
                .replace("&nbsp;", " ").replace("&#160;", " ").replace(' ', ' ')
                .isBlank();
    }
}
