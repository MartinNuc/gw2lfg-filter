package cz.nuc.gw2lfg;

/**
 * Created with IntelliJ IDEA.
 * User: mist
 * Date: 13.4.13
 * Time: 0:02
 * To change this template use File | Settings | File Templates.
 */
public class LookingForGroupItem {
    private String name;
    private Integer level;
    private String text;
    private String updated;
    private String event;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(String level) {
        try {
            this.level = Integer.parseInt(level);
        } catch (Exception e) {
            this.level = null;
        }
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUpdated() {
        if (updated.equalsIgnoreCase("less than a minute")) {
            return "<1 minute";
        }
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LookingForGroupItem lookingForGroupItem = (LookingForGroupItem) o;

        if (level != null ? !level.equals(lookingForGroupItem.level) : lookingForGroupItem.level != null) return false;
        if (!name.equals(lookingForGroupItem.name)) return false;
        if (!text.equals(lookingForGroupItem.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + text.hashCode();
        return result;
    }
}
