package com.sd.mvc.intercept_video_link.bean;

public class ZhihuVideoBean {

    /**
     * playlist : {"LD":{"format":"mp4","play_url":"https://vdn2.vzuu.com/LD/3f0ab108-f50e-11e8-a8c9-0a580a4bac0e.mp4?disable_local_cache=1&bu=com&expiration=1551082984&auth_key=1551082984-0-0-b3f866f6d2302a5e227e38e1edd3c3f8&f=mp4&v=bsy","height":360,"width":640,"fps":25,"duration":46.383,"bitrate":372.013,"size":2156886}}
     * cover_url : https://pic2.zhimg.com/v2-ccc07618f6b5e8dc4ae63462831edab1.jpg
     * title :
     */

    private PlaylistBean playlist;
    private String cover_url;
    private String title;

    public PlaylistBean getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlaylistBean playlist) {
        this.playlist = playlist;
    }

    public String getCover_url() {
        return cover_url;
    }

    public void setCover_url(String cover_url) {
        this.cover_url = cover_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static class PlaylistBean {
        /**
         * LD : {"format":"mp4","play_url":"https://vdn2.vzuu.com/LD/3f0ab108-f50e-11e8-a8c9-0a580a4bac0e.mp4?disable_local_cache=1&bu=com&expiration=1551082984&auth_key=1551082984-0-0-b3f866f6d2302a5e227e38e1edd3c3f8&f=mp4&v=bsy","height":360,"width":640,"fps":25,"duration":46.383,"bitrate":372.013,"size":2156886}
         */

        private LDBean LD;

        public LDBean getLD() {
            return LD;
        }

        public void setLD(LDBean LD) {
            this.LD = LD;
        }

        public static class LDBean {
            /**
             * format : mp4
             * play_url : https://vdn2.vzuu.com/LD/3f0ab108-f50e-11e8-a8c9-0a580a4bac0e.mp4?disable_local_cache=1&bu=com&expiration=1551082984&auth_key=1551082984-0-0-b3f866f6d2302a5e227e38e1edd3c3f8&f=mp4&v=bsy
             * height : 360
             * width : 640
             * fps : 25.0
             * duration : 46.383
             * bitrate : 372.013
             * size : 2156886
             */

            private String format;
            private String play_url;
            private int height;
            private int width;
            private double fps;
            private double duration;
            private double bitrate;
            private int size;

            public String getFormat() {
                return format;
            }

            public void setFormat(String format) {
                this.format = format;
            }

            public String getPlay_url() {
                return play_url;
            }

            public void setPlay_url(String play_url) {
                this.play_url = play_url;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public double getFps() {
                return fps;
            }

            public void setFps(double fps) {
                this.fps = fps;
            }

            public double getDuration() {
                return duration;
            }

            public void setDuration(double duration) {
                this.duration = duration;
            }

            public double getBitrate() {
                return bitrate;
            }

            public void setBitrate(double bitrate) {
                this.bitrate = bitrate;
            }

            public int getSize() {
                return size;
            }

            public void setSize(int size) {
                this.size = size;
            }
        }
    }
}
