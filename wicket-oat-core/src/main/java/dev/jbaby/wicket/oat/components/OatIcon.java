package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An icon, drawn as inline SVG in the current text color and sized to the text (1em),
 * from a set of 64 common business icons from <a href="https://lucide.dev">Lucide</a>
 * (ISC license, see {@code META-INF/LICENSE-lucide.txt}): {@code plus}, {@code pencil},
 * {@code trash-2}, {@code search}, {@code download}, {@code calendar}, {@code user},
 * {@code settings}, {@code chart-line}, ... - see {@link #names()}.
 * <pre>{@code
 * <a wicket:id="export" class="button"><svg wicket:id="icon"></svg> Export</a>
 *
 * link.add(new OatIcon("icon", "download"));
 * }</pre>
 * Put it on an {@code <svg>} tag, or on any other tag to get the SVG inside it. An icon
 * next to text is decorative and hidden from screen readers; an icon on its own (an
 * icon-only button) needs a {@link #setLabel label}. Register icons of your own, with
 * the same 24×24 stroke style, with {@link #register}. No image requests, no icon font,
 * and nothing the Content Security Policy needs to allow.
 */
public class OatIcon extends WebComponent {

    private static final Map<String, String> ICONS = new ConcurrentHashMap<>(load());

    private IModel<String> label;

    /** @param name an icon's name, e.g. {@code "pencil"} */
    public OatIcon(String id, String name) {
        this(id, Model.of(name));
    }

    /** @param name an icon's name, read on every render, e.g. to switch between icons */
    public OatIcon(String id, IModel<String> name) {
        super(id, name);
        if (name.getObject() != null && !ICONS.containsKey(name.getObject())) {
            throw new IllegalArgumentException("No icon \"" + name.getObject() + "\"; the icons are " + names());
        }
    }

    /** A name for screen readers, for an icon that stands on its own; {@code null} makes it decorative. */
    public OatIcon setLabel(IModel<String> label) {
        this.label = label;
        return this;
    }

    public OatIcon setLabel(String label) {
        return setLabel(Model.of(label));
    }

    /** The names of all icons, built-in and registered. */
    public static Set<String> names() {
        return Collections.unmodifiableSet(new TreeSet<>(ICONS.keySet()));
    }

    /**
     * Adds an icon or replaces one: {@code shapes} is the SVG content of a 24×24 icon
     * drawn with strokes, e.g. {@code <path d="M5 12h14"/>}. It is written into the page
     * as it is, so only register shapes you trust.
     */
    public static void register(String name, String shapes) {
        ICONS.put(name, shapes);
    }

    private String shapes() {
        String name = getDefaultModelObjectAsString();
        String shapes = ICONS.get(name);
        if (shapes == null) {
            throw new IllegalStateException("No icon \"" + name + "\"; the icons are " + names());
        }
        return shapes;
    }

    private String svgAttributes() {
        String text = label != null ? label.getObject() : null;
        StringBuilder attributes = new StringBuilder(" class=\"oat-icon\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\""
                + " fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"");
        if (text != null) {
            attributes.append(" role=\"img\" aria-label=\"")
                    .append(org.apache.wicket.util.string.Strings.escapeMarkup(text)).append('"');
        } else {
            attributes.append(" aria-hidden=\"true\" focusable=\"false\"");
        }
        return attributes.toString();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if ("svg".equalsIgnoreCase(tag.getName())) {
            for (String attribute : new String[] {"viewBox", "fill", "stroke", "stroke-width", "stroke-linecap", "stroke-linejoin"}) {
                tag.remove(attribute);
            }
            tag.append("class", "oat-icon", " ");
            tag.put("xmlns", "http://www.w3.org/2000/svg");
            tag.put("viewBox", "0 0 24 24");
            tag.put("fill", "none");
            tag.put("stroke", "currentColor");
            tag.put("stroke-width", "2");
            tag.put("stroke-linecap", "round");
            tag.put("stroke-linejoin", "round");
            String text = label != null ? label.getObject() : null;
            if (text != null) {
                tag.put("role", "img");
                tag.put("aria-label", text);
            } else {
                tag.put("aria-hidden", "true");
                tag.put("focusable", "false");
            }
        }
        if (tag.isOpenClose()) {
            tag.setType(org.apache.wicket.markup.parser.XmlTag.TagType.OPEN);
        }
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        String body = "svg".equalsIgnoreCase(openTag.getName()) ? shapes() : "<svg" + svgAttributes() + ">" + shapes() + "</svg>";
        replaceComponentTagBody(markupStream, openTag, body);
    }

    @Override
    protected void onDetach() {
        if (label != null) {
            label.detach();
        }
        super.onDetach();
    }

    private static Map<String, String> load() {
        Properties properties = new Properties();
        try (InputStream in = OatIcon.class.getResourceAsStream("icons/lucide.properties")) {
            if (in == null) {
                throw new IllegalStateException("icons/lucide.properties is missing");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Map<String, String> icons = new ConcurrentHashMap<>();
        properties.stringPropertyNames().forEach(name -> icons.put(name, properties.getProperty(name)));
        return icons;
    }
}
