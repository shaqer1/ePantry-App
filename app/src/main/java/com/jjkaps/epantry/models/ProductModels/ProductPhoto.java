package com.jjkaps.epantry.models.ProductModels;

import java.io.Serializable;
import java.util.Date;

public class ProductPhoto implements Serializable {

    private String small;
    private String thumb;
    private String display;
    private String userImage;
    private Date userImageDateModified;
    private boolean customImage;

    public ProductPhoto(){}

    public ProductPhoto(String small, String thumb, String display) {
        this.small = small;
        this.thumb = thumb;
        this.display = display;
        this.userImage = null;
        this.userImageDateModified = null;
        this.customImage = false;
    }

    public String getSmall() {
        return small;
    }

    public String getThumb() {
        return thumb;
    }

    public String getDisplay() {
        return display;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public Date getUserImageDateModified() {
        return userImageDateModified;
    }

    public void setUserImageDateModified(Date userImageDateModified) {
        this.userImageDateModified = userImageDateModified;
    }

    public boolean isCustomImage() {
        return customImage;
    }

    public void setCustomImage(boolean customImage) {
        this.customImage = customImage;
    }
}
