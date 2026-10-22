package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.WicketOats;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Renders as an Oat Avatar: {@code <figure data-variant="avatar">} with an {@code <img>},
 * text initials in an {@code <abbr>}, or both. With both, the picture covers the
 * initials, which show while it loads and if it can't be loaded (Wicket Oat's script,
 * {@link WicketOats#WICKET_OAT_JS}, hides a picture that fails, and makes one that loaded
 * opaque, so the initials don't show through it). The models are read once on every
 * render, so an avatar follows them, e.g. in a reused list row.
 * <pre>{@code
 * new OatAvatar("avatar", "AL");                                        // initials
 * new OatAvatar("avatar", person.map(Person::pictureUrl));              // a picture
 * new OatAvatar("avatar", person.map(Person::pictureUrl),
 *         person.map(Person::name).map(OatAvatar::initialsOf), OatAvatar.Size.SMALL);
 * OatAvatar.initials("avatar", person.map(Person::initials));           // initials from a model
 * }</pre>
 */
public class OatAvatar extends Panel {

    // Any space, such as the ideographic space in Japanese names
    private static final Pattern SPACES = Pattern.compile("[\\s\\p{Z}]+");

    public enum Size {
        SMALL("small"),
        DEFAULT(null),
        LARGE("large");

        private final String className;
        Size(String className) { this.className = className; }
        public String getClassName() { return className; }
    }

    private final IModel<String> imageUrl;
    private final IModel<String> initials;
    private final WebMarkupContainer img;
    private final Label label;

    /** A picture; {@code imageUrl} is its URL. */
    public OatAvatar(String id, IModel<String> imageUrl) {
        this(id, imageUrl, null, Size.DEFAULT);
    }

    public OatAvatar(String id, String initials) {
        this(id, null, Model.of(initials), Size.DEFAULT);
    }

    /**
     * @param imageUrl the picture's URL, or {@code null} (or a model of {@code null} or a
     *                 blank URL) for none
     * @param initials the initials, shown without a picture or under it; or {@code null}
     */
    public OatAvatar(String id, IModel<String> imageUrl, IModel<String> initials, Size size) {
        super(id);
        setRenderBodyOnly(true);
        this.imageUrl = OncePerRequestModel.of(imageUrl);
        this.initials = OncePerRequestModel.of(initials);

        WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        if (size != Size.DEFAULT) {
            container.add(AttributeModifier.append("class", size.getClassName()));
        }

        img = new WebMarkupContainer("img");
        img.add(AttributeModifier.replace("src", (IModel<String>) this::getImageUrl));
        // With initials under it, the picture adds nothing for screen readers, and a
        // picture that can't be loaded shows nothing rather than a broken image
        img.add(AttributeModifier.replace("alt", (IModel<String>) () -> hasInitials() ? "" : null));
        container.add(img);

        label = new Label("initials", (IModel<String>) this::getInitials);
        label.add(AttributeModifier.replace("title", (IModel<String>) this::getInitials));
        container.add(label);
    }

    /** Initials from a model, e.g. {@code OatAvatar.initials(id, person.map(Person::initials))}. */
    public static OatAvatar initials(String id, IModel<String> initials) {
        return new OatAvatar(id, null, initials, Size.DEFAULT);
    }

    /** Initials for a name: "Jordan Lee" gives "JL", "ada" gives "A", no name "?". */
    public static String initialsOf(String name) {
        List<String> words = SPACES.splitAsStream(name != null ? name : "").filter(word -> !word.isEmpty()).toList();
        if (words.isEmpty()) {
            return "?";
        }
        String first = firstCharacter(words.get(0));
        String last = words.size() > 1 ? firstCharacter(words.get(words.size() - 1)) : "";
        return (first + last).toUpperCase(Locale.ROOT);
    }

    // Whole, also when it is outside the Basic Multilingual Plane (two chars)
    private static String firstCharacter(String word) {
        return word.substring(0, word.offsetByCodePoints(0, 1));
    }

    private String getImageUrl() {
        String url = imageUrl != null ? imageUrl.getObject() : null;
        return Strings.isEmpty(url) ? null : url;
    }

    private String getInitials() {
        return initials != null ? initials.getObject() : null;
    }

    private boolean hasInitials() {
        return getInitials() != null;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        boolean hasImage = getImageUrl() != null;
        img.setVisible(hasImage);
        label.setVisible(!hasImage || hasInitials());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (getImageUrl() != null) {
            // Hides a picture that can't be loaded: the initials, or nothing, instead of a broken image
            response.render(JavaScriptHeaderItem.forReference(WicketOats.WICKET_OAT_JS));
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (imageUrl != null) {
            imageUrl.detach();
        }
        if (initials != null) {
            initials.detach();
        }
    }
}
