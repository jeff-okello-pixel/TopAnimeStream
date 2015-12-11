package com.topanimestream.utilities;

public class ImageUtils {
    public static enum ImageSize{
        w45("45"),
        w92("92"),
        w154("154"),
        w185("185"),
        w300("300"),
        w500("500"),
        w780("780"),
        w1280("1280");
        private final String value;

        private ImageSize(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }
    public static String resizeImage(String image, int size) {
        if (image == null)
            return null;

        if (size == 0 || size > 2000)
            return null;

        //original size
        if(size == -1)
            return image;

        return image + "?width=" + size;
    }
}
