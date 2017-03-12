package com.lightmsg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;

import com.lightmsg.R;

/**
 * MediaScanner helper class. {@hide}
 */
public class MediaFile {
    private final static String TAG = "MediaFile";
    private final static boolean DEBUG = true;
    // comma separated list of all file extensions supported by the media
    // scanner
    public static String sFileExtensions;
    // Audio file types
    public static final int FILE_TYPE_MP3 = 1;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_OGG = 7;
    public static final int FILE_TYPE_AAC = 8;
    public static final int FILE_TYPE_3GA = 9;
    public static final int FILE_TYPE_FLAC = 10;
    // NTT_NEXTI_PV_PLAYREADY-S
    public static final int FILE_TYPE_M4B = 11;

    // dongju SISO DRM Start for VideoStore
    public static final int FILE_TYPE_PYA = 12;

    // NTT_NEXTI_PV_PLAYREADY-S
    public static final int FILE_TYPE_ISMA = 13;
    // dongju SISO DRM END
    public static final int FILE_TYPE_MP4_AUDIO = 16;
    public static final int FILE_TYPE_3GP_AUDIO = 17;
    public static final int FILE_TYPE_3G2_AUDIO = 18;
    public static final int FILE_TYPE_ASF_AUDIO = 19;
    public static final int FILE_TYPE_3GPP_AUDIO = 20;

    // 2012.03.08 TOD_ghLim : add the qcp type
    public static final int FILE_TYPE_QCP = 21;

    private static final int FIRST_AUDIO_FILE_TYPE = FILE_TYPE_MP3;
    private static final int LAST_AUDIO_FILE_TYPE = FILE_TYPE_QCP;
    // MIDI file types
    public static final int FILE_TYPE_MID = 22;
    public static final int FILE_TYPE_SMF = 23;
    public static final int FILE_TYPE_IMY = 24;
    // NAGSM_Android_HQ_MX_soomyung.hyun_20100406 START [USA_ATT] add the
    // sp-midi type
    public static final int FILE_TYPE_SPM = 25;
    // public static final int FILE_TYPE_MIDI = 24; // zerolism
    private static final int FIRST_MIDI_FILE_TYPE = FILE_TYPE_MID;
    // NAGSM_Android_HQ_MX_soomyung.hyun_20100406 START [USA_ATT] add the
    // sp-midi type
    private static final int LAST_MIDI_FILE_TYPE = FILE_TYPE_SPM;
    // private static final int LAST_MIDI_FILE_TYPE = FILE_TYPE_IMY;
    // private static final int LAST_MIDI_FILE_TYPE = FILE_TYPE_MIDI;
    // Video file types
    public static final int FILE_TYPE_MP4 = 31;
    public static final int FILE_TYPE_M4V = 32;
    public static final int FILE_TYPE_3GPP = 33;
    public static final int FILE_TYPE_3GPP2 = 34;
    public static final int FILE_TYPE_WMV = 35;
    public static final int FILE_TYPE_MPG = 36;
    public static final int FILE_TYPE_ASF = 37;
    public static final int FILE_TYPE_AVI = 38;
    public static final int FILE_TYPE_DIVX = 39;
    public static final int FILE_TYPE_FLV = 40;
    public static final int FILE_TYPE_MKV = 41;

    public static final int FILE_TYPE_MOV = 42;

    // dongju SISO DRM for VideoStore - start
    public static final int FILE_TYPE_PYV = 43;
    // dongju SISO DRM for VideoStore - end
    // DSKIM 2011.03.16 Video type for U1_KOR_SKT --START
    public static final int FILE_TYPE_SKM = 44;
    public static final int FILE_TYPE_K3G = 45;
    public static final int FILE_TYPE_AK3G = 46;
    // DSKIM 2011.03.16 Video type for U1_KOR_SKT --END

    // 2011.08.25 TOD_ghLim : WEBM added.
    public static final int FILE_TYPE_WEBM = 47;

    // yongqing.ni Add rm/rmvb support(China feature) --START
    public static final int FILE_TYPE_RM = 48;

    public static final int FILE_TYPE_RMVB = 49;
    // yongqing.ni Add rm/rmvb support(China feature) --END
    public static final int FILE_TYPE_SDP = 50;

    // NTT_NEXTI_PV_PLAYREADY-S
    public static final int FILE_TYPE_ISMV = 51;

    private static final int FIRST_VIDEO_FILE_TYPE = FILE_TYPE_MP4;
    private static final int LAST_VIDEO_FILE_TYPE = FILE_TYPE_ISMV;
    // Image file types
    public static final int FILE_TYPE_JPEG = 61;
    public static final int FILE_TYPE_GIF = 62;
    public static final int FILE_TYPE_PNG = 63;
    public static final int FILE_TYPE_BMP = 64;
    public static final int FILE_TYPE_WBMP = 65;
    public static final int FILE_TYPE_DCF_I = 66;

    private static final int FIRST_IMAGE_FILE_TYPE = FILE_TYPE_JPEG;
    private static final int LAST_IMAGE_FILE_TYPE = FILE_TYPE_DCF_I;
    // Playlist file types
    public static final int FILE_TYPE_M3U = 71;
    public static final int FILE_TYPE_PLS = 72;
    public static final int FILE_TYPE_WPL = 73;
    private static final int FIRST_PLAYLIST_FILE_TYPE = FILE_TYPE_M3U;
    private static final int LAST_PLAYLIST_FILE_TYPE = FILE_TYPE_WPL;
    // Documents
    public static final int FILE_TYPE_ASC = 78;
    public static final int FILE_TYPE_PPS = 79;
    public static final int FILE_TYPE_CSV = 80;
    public static final int FILE_TYPE_PDF = 81;
    public static final int FILE_TYPE_DOC = 82;
    public static final int FILE_TYPE_XLS = 83;
    public static final int FILE_TYPE_PPT = 84;
    public static final int FILE_TYPE_TXT = 85;
    public static final int FILE_TYPE_GUL = 86;
    private static final int FIRST_DOCUMENT_FILE_TYPE = FILE_TYPE_ASC;
    private static final int LAST_DOCUMENT_FILE_TYPE = FILE_TYPE_GUL;
    
    public static final int FILE_TYPE_DCF = 87;
    public static final int FILE_TYPE_ODF = 88;

    public static final int FILE_TYPE_EBOOK = 89; // 2011. 08. 05 yhPark :
                                                  // e-book added
    private static final int FIRST_DRM_FILE_TYPE = FILE_TYPE_DCF;
    private static final int LAST_DRM_FILE_TYPE = FILE_TYPE_ODF;
    // public static final int FILE_TYPE_QSS = 86;// pineone:zerolism 090820
    // private static final int LAST_SLIDE_FILE_TYPE = FILE_TYPE_QSS;
    // Flash files
    public static final int FILE_TYPE_SWF = 90;
    public static final int FILE_TYPE_SVG = 91;
    private static final int FIRST_FLASH_FILE_TYPE = FILE_TYPE_SWF;
    private static final int LAST_FLASH_FILE_TYPE = FILE_TYPE_SVG;
    // install files
    public static final int FILE_TYPE_APK = 100;
    // WAC_WRT - added new file type for installation (raghu.tn@samsung.com)
    public static final int FILE_TYPE_WGT = 101;
    private static final int FIRST_INSTALL_FILE_TYPE = FILE_TYPE_APK;
    private static final int LAST_INSTALL_FILE_TYPE = FILE_TYPE_WGT; // FILE_TYPE_APK;
                                                                     // - allow
                                                                     // WGT file
    // javaME files
    public static final int FILE_TYPE_JAD = 110;
    public static final int FILE_TYPE_JAR = 111;
    private static final int FIRST_JAVA_FILE_TYPE = FILE_TYPE_JAD;
    private static final int LAST_JAVA_FILE_TYPE = FILE_TYPE_JAR;
    // vnote, vcalender
    public static final int FILE_TYPE_VCS = 120; // vCalendar
    public static final int FILE_TYPE_VCF = 121; // vCard
    public static final int FILE_TYPE_VNT = 122; // vNote
    public static final int FILE_TYPE_VTS = 123; // Task
    // private static final int FIRST_VOBJECT_TYPE = FILE_TYPE_VCS;
    //
    // private static final int LAST_VOBJECT_TYPE = FILE_TYPE_VNT;
    // html
    public static final int FILE_TYPE_HTML = 126;
    public static final int FILE_TYPE_XML = 127;
    public static final int FILE_TYPE_XHTML = 128;
    public static final int FILE_TYPE_MHTML = 129;

    // 2011.08.25 TOD_ghLim : hwp added.
    public static final int FILE_TYPE_HWP = 141;
    public static final int FILE_TYPE_EML = 142;
    public static final int FILE_TYPE_ZIP = 143;
    public static final int FILE_TYPE_SNB = 144;
    public static final int FILE_TYPE_SSF = 145;
    public static final int FILE_TYPE_WEBP = 146;
    public static final int FILE_TYPE_SPD = 147;
    public static final int FILE_TYPE_SCC = 148;
    public static final int FILE_TYPE_GOLF= 149;

    public static final int FILE_TYPE_P12 = 150;

    public static final int FILE_ICON_DEFAULT_SMALL = R.drawable.mime_detail_ic_etc;
    public static final int FILE_ICON_DEFAULT_LARGE = R.drawable.mime_detail_ic_etc;

    static class MediaFileType {
        int fileType;
        String mimeType;
        String description;
        int iconSmall;
        int iconLarge;

        MediaFileType(int fileType, String mimeType, String desc, int iconSmall, int iconLarge) {
            this.fileType = fileType;
            this.mimeType = mimeType;
            this.description = desc;
            this.iconSmall = iconSmall;
            this.iconLarge = iconLarge;
        }
    }

    private static HashMap<String, MediaFileType> sFileTypeMap = new HashMap<String, MediaFileType>();

    private static HashMap<String, Integer> sMimeTypeMap = new HashMap<String, Integer>();

    private static HashMap<String, String> sMimeType = new HashMap<String, String>();

    static void addFileType(String extension, int fileType, String mimeType, String desc,
            int iconSmall, int iconLarge) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType, desc, iconSmall,
                iconLarge));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
        sMimeType.put(extension, mimeType);
    }

    static {
        //addFileType(extension, fileType, mimeType, desc, iconSmall, iconLarge)
        addFileType("EML", FILE_TYPE_EML, "message/rfc822", "EML", R.drawable.mime_detail_ic_eml, 0);
        addFileType("MP3", FILE_TYPE_MP3, "audio/mpeg", "Mpeg", R.drawable.mime_detail_ic_music, 0);
        addFileType("M4A", FILE_TYPE_M4A, "audio/mp4", "M4A", R.drawable.mime_detail_ic_amr, 0);
        addFileType("WAV", FILE_TYPE_WAV, "audio/x-wav", "WAVE", R.drawable.mime_detail_ic_music, 0);
        addFileType("AMR", FILE_TYPE_AMR, "audio/amr", "AMR", R.drawable.mime_detail_ic_amr, 0);
        addFileType("AWB", FILE_TYPE_AWB, "audio/amr-wb", "AWB", R.drawable.mime_detail_ic_amr, 0);
        addFileType("WMA", FILE_TYPE_WMA, "audio/x-ms-wma", "WMA", R.drawable.mime_detail_ic_music, 0);
        addFileType("OGG", FILE_TYPE_OGG, "audio/ogg", "OGG", R.drawable.mime_detail_ic_music, 0);
        addFileType("OGA", FILE_TYPE_OGG, "audio/ogg", "OGA", R.drawable.mime_detail_ic_music, 0);
        addFileType("AAC", FILE_TYPE_AAC, "audio/aac", "AAC", R.drawable.mime_detail_ic_music, 0);

        addFileType("3GA", FILE_TYPE_3GA, "audio/3gpp", "3GA", R.drawable.mime_detail_ic_amr, 0);
        addFileType("FLAC", FILE_TYPE_FLAC, "audio/flac", "FLAC", R.drawable.mime_detail_ic_music, 0);
        addFileType("MPGA", FILE_TYPE_MP3, "audio/mpeg", "MPGA", R.drawable.mime_detail_ic_music, 0);
        addFileType("MP4_A", FILE_TYPE_MP4_AUDIO, "audio/mp4", "MP4 Audio", R.drawable.mime_detail_ic_music, 0);
        addFileType("3GP_A", FILE_TYPE_3GP_AUDIO, "audio/3gpp", "3GP Audio", R.drawable.mime_detail_ic_music, 0);
        addFileType("3G2_A", FILE_TYPE_3G2_AUDIO, "audio/3gpp2", "3G2 Audio", R.drawable.mime_detail_ic_music, 0);
        addFileType("ASF_A", FILE_TYPE_ASF_AUDIO, "audio/x-ms-asf", "ASF Audio", R.drawable.mime_detail_ic_music, 0);
        addFileType("3GPP_A", FILE_TYPE_3GPP_AUDIO, "audio/3gpp", "3GPP", R.drawable.mime_detail_ic_music, 0);
        addFileType("MID", FILE_TYPE_MID, "audio/midi", "MIDI", R.drawable.mime_detail_ic_music, 0);
        addFileType("XMF", FILE_TYPE_MID, "audio/midi", "XMF", R.drawable.mime_detail_ic_music, 0);

        addFileType("MXMF", FILE_TYPE_MID, "audio/midi", "MXMF", R.drawable.mime_detail_ic_music, 0);
        addFileType("RTTTL", FILE_TYPE_MID, "audio/midi", "RTTTL", R.drawable.mime_detail_ic_music, 0);
        addFileType("SMF", FILE_TYPE_SMF, "audio/sp-midi", "SMF", R.drawable.mime_detail_ic_music, 0);
        addFileType("IMY", FILE_TYPE_IMY, "audio/imelody", "IMY", R.drawable.mime_detail_ic_music, 0);
        addFileType("MIDI", FILE_TYPE_MID, "audio/midi", "MIDI", R.drawable.mime_detail_ic_music, 0);
        addFileType("RTX", FILE_TYPE_MID, "audio/midi", "MIDI", R.drawable.mime_detail_ic_music, 0);
        addFileType("OTA", FILE_TYPE_MID, "audio/midi", "MIDI", R.drawable.mime_detail_ic_music, 0);
        addFileType("PYA", FILE_TYPE_PYA, "audio/vnd.ms-playready.media.pya", "PYA", R.drawable.mime_detail_ic_music, 0);
        addFileType("QCP", FILE_TYPE_QCP, "audio/qcelp", "QCP", R.drawable.mime_detail_ic_music, 0);
        addFileType("MPEG", FILE_TYPE_MPG, "video/mpeg", "MPEG", R.drawable.mime_detail_ic_video, 0);

        addFileType("MPG", FILE_TYPE_MPG, "video/mpeg", "MPEG", R.drawable.mime_detail_ic_video, 0);
        addFileType("MP4", FILE_TYPE_MP4, "video/mp4", "MP4", R.drawable.mime_detail_ic_video, 0);
        addFileType("M4V", FILE_TYPE_M4V, "video/mp4", "M4V", R.drawable.mime_detail_ic_video, 0);
        addFileType("3GP", FILE_TYPE_3GPP, "video/3gpp", "3GP", R.drawable.mime_detail_ic_video, 0);
        addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp", "3GPP", R.drawable.mime_detail_ic_video, 0);
        addFileType("3G2", FILE_TYPE_3GPP2, "video/3gpp2", "3G2", R.drawable.mime_detail_ic_video, 0);
        addFileType("3GPP2", FILE_TYPE_3GPP2, "video/3gpp2", "3GPP2", R.drawable.mime_detail_ic_video, 0);
        addFileType("WMV", FILE_TYPE_WMV, "video/x-ms-wmv", "WMV", R.drawable.mime_detail_ic_video, 0);
        addFileType("ASF", FILE_TYPE_ASF, "video/x-ms-asf", "ASF", R.drawable.mime_detail_ic_video, 0);
        addFileType("AVI", FILE_TYPE_AVI, "video/avi", "AVI", R.drawable.mime_detail_ic_video, 0);

        addFileType("DIVX", FILE_TYPE_DIVX, "video/divx", "DIVX", R.drawable.mime_detail_ic_video, 0);
        addFileType("FLV", FILE_TYPE_FLV, "video/flv", "FLV", R.drawable.mime_detail_ic_video, 0);
        addFileType("MKV", FILE_TYPE_MKV, "video/mkv", "MKV", R.drawable.mime_detail_ic_video, 0);
        addFileType("SDP", FILE_TYPE_SDP, "application/sdp", "SDP", R.drawable.mime_detail_ic_video, 0);
        addFileType("MOV", FILE_TYPE_MOV, "video/quicktime", "MOV", R.drawable.mime_detail_ic_video, 0);
        addFileType("PYV", FILE_TYPE_PYV, "video/vnd.ms-playready.media.pyv", "PYV", R.drawable.mime_detail_ic_video, 0);
        addFileType("WEBM", FILE_TYPE_WEBM, "video/webm", "WEBM", R.drawable.mime_detail_ic_video, 0);
        addFileType("JPG", FILE_TYPE_JPEG, "image/jpeg", "JPEG", R.drawable.mime_detail_ic_images, 0);
        addFileType("JPEG", FILE_TYPE_JPEG, "image/jpeg", "JPEG", R.drawable.mime_detail_ic_images, 0);
        addFileType("DCF_I", FILE_TYPE_DCF_I, "image/dcf", "DCF Image", R.drawable.mime_detail_ic_images, 0);
        addFileType("MY5", FILE_TYPE_JPEG, "image/vnd.tmo.my5", "JPEG", R.drawable.mime_detail_ic_images, 0);

        addFileType("GIF", FILE_TYPE_GIF, "image/gif", "GIF", R.drawable.mime_detail_ic_images, 0);
        addFileType("PNG", FILE_TYPE_PNG, "image/png", "PNG", R.drawable.mime_detail_ic_images, 0);
        addFileType("BMP", FILE_TYPE_BMP, "image/x-ms-bmp", "Microsoft BMP", R.drawable.mime_detail_ic_images, 0);
        addFileType("WBMP", FILE_TYPE_WBMP, "image/vnd.wap.wbmp", "Wireless BMP", R.drawable.mime_detail_ic_images, 0);
        addFileType("M3U", FILE_TYPE_M3U, "audio/x-mpegurl", "M3U", R.drawable.mime_detail_ic_etc, 0);
        addFileType("PLS", FILE_TYPE_PLS, "audio/x-scpls", "PLS", R.drawable.mime_detail_ic_etc, 0);
        addFileType("WPL", FILE_TYPE_WPL, "application/vnd.ms-wpl", "WPL", R.drawable.mime_detail_ic_etc, 0);
        addFileType("PDF", FILE_TYPE_PDF, "application/pdf", "Acrobat PDF", R.drawable.mime_detail_ic_pdf, 0);
        addFileType("RTF", FILE_TYPE_DOC, "application/msword", "Microsoft Office WORD", R.drawable.mime_detail_ic_rtf, 0);
        addFileType("DOC", FILE_TYPE_DOC, "application/msword", "Microsoft Office WORD", R.drawable.mime_detail_ic_word, 0);
        addFileType("GOLF", FILE_TYPE_GOLF, "image/*", "GOLF", R.drawable.mime_detail_ic_images, 0);

        addFileType("DOCX", FILE_TYPE_DOC,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Microsoft Office WORD", R.drawable.mime_detail_ic_word, 0);
        addFileType("DOT", FILE_TYPE_DOC, "application/msword", "Microsoft Office WORD", R.drawable.mime_detail_ic_word, 0);
        addFileType("DOTX", FILE_TYPE_DOC,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                "Microsoft Office WORD", R.drawable.mime_detail_ic_word, 0);
        addFileType("DOCM", FILE_TYPE_DOC, "application/vnd.ms-word.document.macroEnabled.12",
                "Microsoft Office WORD", R.drawable.mime_detail_ic_word, 0);
        addFileType("DOTM", FILE_TYPE_DOC, "application/vnd.ms-word.template.macroEnabled.12",
                "Microsoft Office WORD", R.drawable.mime_detail_ic_word, 0);
        
        addFileType("CSV", FILE_TYPE_CSV, "text/comma-separated-values", "Microsoft Office Excel",
                R.drawable.mime_detail_ic_xls, 0);
        
        addFileType("XLS", FILE_TYPE_XLS, "application/vnd.ms-excel", "Microsoft Office Excel", R.drawable.mime_detail_ic_xls, 0);
        addFileType("XLT", FILE_TYPE_XLS, "application/vnd.ms-excel", "Microsoft Office Excel", R.drawable.mime_detail_ic_xls, 0);
        addFileType("XLA", FILE_TYPE_XLS, "application/vnd.ms-excel", "Microsoft Office Excel", R.drawable.mime_detail_ic_xls, 0);
        
        addFileType("XLSX", FILE_TYPE_XLS, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "Microsoft Office Excel", R.drawable.mime_detail_ic_xls, 0);
        addFileType("XLTX", FILE_TYPE_XLS, "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                "Microsoft Office Excel", R.drawable.mime_detail_ic_xls, 0);
        addFileType("XLSM", FILE_TYPE_XLS, "application/vnd.ms-excel.sheet.macroEnabled.12",
                "Microsoft Office Excel", R.drawable.mime_detail_ic_xls, 0);
        addFileType("XLTM", FILE_TYPE_XLS, "application/vnd.ms-excel.template.macroEnabled.12",
                "Microsoft Office Excel", R.drawable.mime_detail_ic_xls, 0);
                
        addFileType("PPT", FILE_TYPE_PPT, "application/vnd.ms-powerpoint",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        addFileType("POT", FILE_TYPE_PPT, "application/vnd.ms-powerpoint",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        addFileType("PPS", FILE_TYPE_PPS, "application/vnd.ms-powerpoint",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        addFileType("PPA", FILE_TYPE_PPS, "application/vnd.ms-powerpoint",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        
        addFileType("PPSX", FILE_TYPE_PPT, "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        addFileType("PPTX", FILE_TYPE_PPT, "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        addFileType("POTX", FILE_TYPE_PPT, "application/vnd.openxmlformats-officedocument.presentationml.template",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        
        addFileType("PPAM", FILE_TYPE_PPT, "application/vnd.ms-powerpoint.addin.macroEnabled.12",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        addFileType("PPTM", FILE_TYPE_PPT, "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        addFileType("PPTM", FILE_TYPE_PPT, "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
        addFileType("PPSM", FILE_TYPE_PPT, "application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
                "Microsoft Office PowerPoint", R.drawable.mime_detail_ic_ppt, 0);
                
        addFileType("ASC", FILE_TYPE_ASC, "text/plain", "Text Document", R.drawable.mime_detail_ic_txt, 0);
        addFileType("TXT", FILE_TYPE_TXT, "text/plain", "Text Document", R.drawable.mime_detail_ic_txt, 0);
        addFileType("GUL", FILE_TYPE_GUL, "application/jungumword", "Jungum Word", R.drawable.mime_detail_ic_gul, 0);
        addFileType("EPUB", FILE_TYPE_EBOOK, "application/epub+zip", "eBookReader", R.drawable.mime_detail_ic_txt, 0);
        addFileType("ACSM", FILE_TYPE_EBOOK, "application/vnd.adobe.adept+xml", "eBookReader", R.drawable.mime_detail_ic_txt, 0);
        addFileType("SWF", FILE_TYPE_SWF, "application/x-shockwave-flash", "SWF", R.drawable.mime_detail_ic_mhtml, 0);
        addFileType("SVG", FILE_TYPE_SVG, "image/svg+xml", "SVG", R.drawable.mime_detail_ic_mhtml, 0);

        addFileType("DCF", FILE_TYPE_DCF, "application/vnd.oma.drm.content", "DRM Content", R.drawable.mime_detail_ic_etc, 0);
        addFileType("ODF", FILE_TYPE_ODF, "application/vnd.oma.drm.content", "DRM Content", R.drawable.mime_detail_ic_etc, 0);
        addFileType("APK", FILE_TYPE_APK, "application/apk", "Android package install file", R.drawable.mime_detail_ic_apk, 0);
        addFileType("JAD", FILE_TYPE_JAD, "text/vnd.sun.j2me.app-descriptor", "JAD", R.drawable.mime_detail_ic_etc, 0);
        addFileType("JAR", FILE_TYPE_JAR, "application/java-archive ", "JAR", R.drawable.mime_detail_ic_etc, 0);
        addFileType("VCS", FILE_TYPE_VCS, "text/x-vCalendar", "VCS", R.drawable.mime_detail_ic_calendar, 0);
        addFileType("ICS", FILE_TYPE_VCS, "text/x-vCalendar", "ICS", R.drawable.mime_detail_ic_calendar, 0);
        addFileType("VTS", FILE_TYPE_VTS, "text/x-vtodo", "VTS", R.drawable.mime_detail_ic_calendar, 0);
        addFileType("VCF", FILE_TYPE_VCF, "text/x-vcard", "VCF", R.drawable.mime_detail_ic_contact, 0);
        addFileType("VNT", FILE_TYPE_VNT, "text/x-vnote", "VNT", R.drawable.mime_detail_ic_memo, 0);

        addFileType("HTML", FILE_TYPE_HTML, "text/html", "HTML", R.drawable.mime_detail_ic_html, 0);
        addFileType("HTM", FILE_TYPE_HTML, "text/html", "HTML", R.drawable.mime_detail_ic_html, 0);
        addFileType("XHTML", FILE_TYPE_XHTML, "text/html", "XHTML", R.drawable.mime_detail_ic_html, 0);
        addFileType("XML", FILE_TYPE_XML, "application/xhtml+xml", "XML", R.drawable.mime_detail_ic_html, 0);
        addFileType("MHT", FILE_TYPE_MHTML, "multipart/related", "MHTML", R.drawable.mime_detail_ic_mhtml, 0);
        addFileType("MHTM", FILE_TYPE_MHTML, "multipart/related", "MHTML", R.drawable.mime_detail_ic_mhtml, 0);
        addFileType("MHTML", FILE_TYPE_MHTML, "multipart/related", "MHTML", R.drawable.mime_detail_ic_mhtml, 0);
        addFileType("WGT", FILE_TYPE_WGT, "application/vnd.samsung.widget", "WGT", R.drawable.mime_detail_ic_mhtml, 0);
        addFileType("HWP", FILE_TYPE_HWP, "application/x-hwp", "HWP", R.drawable.mime_detail_ic_hwp, 0);
        addFileType("HWT", FILE_TYPE_HWP, "application/x-hwp", "HWT", R.drawable.mime_detail_ic_hwp, 0);
        addFileType("ZIP", FILE_TYPE_ZIP, "application/zip", "ZIP", R.drawable.mime_detail_ic_compressed, 0);
        addFileType("SNB", FILE_TYPE_SNB, "application/snb", "SNB", R.drawable.mime_detail_ic_snb, 0);
        addFileType("SSF", FILE_TYPE_SSF, "application/ssf", "SSF", R.drawable.mime_detail_ic_story_album, 0);
        addFileType("WEBP", FILE_TYPE_WEBP, "image/webp", "WEBP", R.drawable.mime_detail_ic_images, 0);
        addFileType("SNBKP", FILE_TYPE_SNB, "application/octet-stream", "SNB", R.drawable.mime_detail_ic_snb, 0);
        addFileType("SMBKP", FILE_TYPE_SNB, "application/octet-stream", "SNB", R.drawable.mime_detail_ic_snb, 0);
        addFileType("SPD", FILE_TYPE_SPD, "application/spd", "SPD", R.drawable.mime_detail_ic_s_note, 0);
        addFileType("SCC", FILE_TYPE_SCC, "application/scc", "SCC", R.drawable.mime_detail_ic_scc, 0);

        addFileType("PFX", FILE_TYPE_P12, "application/x-pkcs12", "PFX", R.drawable.mime_detail_ic_etc, 0);
        addFileType("P12", FILE_TYPE_P12, "application/x-pkcs12", "P12", R.drawable.mime_detail_ic_etc, 0);

        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = sFileTypeMap.keySet().iterator();
        while (iterator.hasNext()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(iterator.next());
        }
        sFileExtensions = builder.toString();
    }

    public static final String UNKNOWN_STRING = "<unknown>";

    public static final String VIDEO_UNSPECIFIED = "video/*";
    public static final String AUDIO_UNSPECIFIED = "audio/*";

    public static boolean isAudioFileType(int fileType) {
        return ((fileType >= FIRST_AUDIO_FILE_TYPE && fileType <= LAST_AUDIO_FILE_TYPE) || (fileType >= FIRST_MIDI_FILE_TYPE && fileType <= LAST_MIDI_FILE_TYPE));
    }

    public static boolean isVideoFileType(int fileType) {
        return (fileType >= FIRST_VIDEO_FILE_TYPE && fileType <= LAST_VIDEO_FILE_TYPE);
    }

    public static boolean isImageFileType(int fileType) {
        return (fileType >= FIRST_IMAGE_FILE_TYPE && fileType <= LAST_IMAGE_FILE_TYPE);
    }

    public static boolean isPlayListFileType(int fileType) {
        return (fileType >= FIRST_PLAYLIST_FILE_TYPE && fileType <= LAST_PLAYLIST_FILE_TYPE);
    }

    public static boolean isDocumentFileType(int fileType) {
        return (fileType >= FIRST_DOCUMENT_FILE_TYPE && fileType <= LAST_DOCUMENT_FILE_TYPE);
    }

    public static boolean isFlashFileType(int fileType) {
        return (fileType >= FIRST_FLASH_FILE_TYPE && fileType <= LAST_FLASH_FILE_TYPE);
    }

    public static boolean isInstallFileType(int fileType) {
        return (fileType >= FIRST_INSTALL_FILE_TYPE && fileType <= LAST_INSTALL_FILE_TYPE);
    }

    public static boolean isJavaFileType(int fileType) {
        return (fileType >= FIRST_JAVA_FILE_TYPE && fileType <= LAST_JAVA_FILE_TYPE);
    }

    public static boolean isDrmFileType(int fileType) {
        return (fileType >= FIRST_DRM_FILE_TYPE && fileType <= LAST_DRM_FILE_TYPE);
    }

    public static boolean isMIDFileType(int fileType) {
        return (fileType >= FIRST_MIDI_FILE_TYPE && fileType <= LAST_MIDI_FILE_TYPE);
    }

    // dongju SISO DRM start : add WMDRM fileType
    public static boolean isWmFileType(int fileType) {
        return (fileType == FILE_TYPE_WMA || fileType == FILE_TYPE_WMV);
    }

    // dongju SISO DRM end : add WMDRM fileType
    // dongju SISO DRM start
    public static boolean isPlayReadyType(int fileType) {
        return (fileType == FILE_TYPE_PYA || fileType == FILE_TYPE_PYV);
    }

    public static boolean isVCFType(int fileType) {
        return (fileType == FILE_TYPE_VCF);
    }

    // dongju SISO DRM end
    public static boolean needThumbnail(String name) {
        int fileType = getFileTypeInt(name);
        return isImageFileType(fileType) || isVideoFileType(fileType)
                || isInstallFileType(fileType);
    }
    
    public static MediaFileType getFileType(String path, Context context, String mimeType) {
        String ext = getExtension(path);
        if (ext == null)
            return null;

        MediaFileType mediaType = sFileTypeMap.get(ext);
        if (needToCheckMimeType(path) && !"SCC".equals(ext)) {
            if (isAudioInMediaStore(path, context)) {
                mediaType = sFileTypeMap.get(ext + "_A");
            }
            if (mimeType.startsWith("image")){
                mediaType = sFileTypeMap.get(ext + "_I");
            }
        }

        return mediaType;
    }

    public static MediaFileType getFileType(String path) {
        String ext = getExtension(path);
        if (ext == null)
            return null;
        MediaFileType mediaType = sFileTypeMap.get(ext);
        
        return mediaType;
    }

    public static int getFileTypeInt(String path) {
        MediaFileType mediaType = getFileType(path);
        return (mediaType == null ? 0 : mediaType.fileType);
    }

    public static String getMimeType(String path) {
        Log.d(TAG, "getMimeType(), path="+path);
        MediaFileType mediaType = getFileType(path);

        // // Support DRM
        // if ( mediaType != null &&
        // mediaType.mimeType.equals("application/vnd.oma.drm.content")){
        //
        // String mimeType = null;
        //
        // try {
        // OMADRMManager manager = OMADRMManager.getInstance();
        // mimeType = manager.getMimeType(path);
        // } catch (OMADRMException drme) {
        // drme.printStackTrace();
        // } catch (IOException ioe) {
        // ioe.printStackTrace();
        // }
        // return mimeType;
        // }

        return (mediaType == null ? "" : mediaType.mimeType);
    }

    public static String getShareMimeType(String path) {
        MediaFileType mediaType = getFileType(path);

        return (mediaType == null ? "application/*" : mediaType.mimeType);
    }

    public static String getShareMimeType(String path, Context context) {
        MediaFileType mediaType;
        String mimeType = null;

        if ((mediaType = getFileType(path)) == null) {
            mimeType = "application/octet-stream";
        } else {
            if (MediaFile.needToCheckMimeType(path)) {
                mimeType = getMimeTypeFromMediaStore(path, context);
            } else {
                mimeType = mediaType.mimeType;
            }
        }
        return mimeType;
    }

    public static String getDescription(String path) {
        MediaFileType mediaType = getFileType(path);
        return (mediaType == null ? "" : mediaType.description);
    }

    public static int getSmallIcon(String path) {
        MediaFileType mediaType = getFileType(path);
        return (mediaType == null ? FILE_ICON_DEFAULT_SMALL : mediaType.iconSmall);
    }
    
    public static int getSmallIcon(String path, String mimeType, Context context) {
        if (needToCheckMimeType(path)) {
            String ext = getExtension(path);

            if ("SCC".equals(ext)) {
                return SCCFileUtil.getSmallIcon(path);
            }
        }

        MediaFileType mediaType = getFileType(path, context, mimeType);
        
        if (mediaType == null && "audio/mp4".equals(mimeType)) {
            String ext = getExtension(path);
            if ("BLOB".equals(ext)) {
                ext = "M4A";
                mediaType = sFileTypeMap.get(ext);
            }
        }
        
        return (mediaType == null ? FILE_ICON_DEFAULT_SMALL : mediaType.iconSmall);
    }

    public static int getSmallIcon(String path, String mimeType) {
        MediaFileType mediaType = null;
        
        if ( needToCheckMimeType( path ) ) {
            
            if(mimeType != null) {
                int intMediaType = MediaFile.getFileTypeForMimeType(mimeType);

                if(MediaFile.isVideoFileType(intMediaType)) {
                    return R.drawable.mime_detail_ic_video;
                } else if(MediaFile.isAudioFileType(intMediaType)) {
                    return R.drawable.mime_detail_ic_music;
                }
            }
            
            String ext = getExtension( path );
            if ( "SCC".equals( ext ) ) {
                return SCCFileUtil.getSmallIcon( path );
            }
        }
        
        mediaType = getFileType(path);
        return (mediaType == null ? FILE_ICON_DEFAULT_SMALL : mediaType.iconSmall);
    }

    public static int getLargeIcon(String path) {
        if ( needToCheckMimeType( path ) ) {
            String ext = getExtension( path );
            
            if ( "SCC".equals( ext ) ) {
                return SCCFileUtil.getLargeIcon( path );
            }
        }        
        MediaFileType mediaType = getFileType(path);
        return (mediaType == null ? FILE_ICON_DEFAULT_LARGE : mediaType.iconLarge);
    }

    public static Drawable getSmallIconDrawable(File f, Activity context) {
        int icon;
//        if (f.isDirectory()) {
//            if (Utils.isExternalRootFolder(f.getAbsolutePath()) || Utils.isUSBStorage(f.getAbsolutePath())) {
//                icon = R.drawable.myfile_folder_sdcard;
//            } else {
//                icon = R.drawable.myfile_folder;
//            }
//        } else {
            // 2012.02.28 TOD_ghLim : distinguish between audio type and video type [
            if (needToCheckMimeType(f.getAbsolutePath())) {
                if (isAudioInMediaStore(f.getAbsolutePath(), context)) {
                    icon = R.drawable.mime_detail_ic_music;
                } else {
                    icon = getSmallIcon(f.getAbsolutePath(), null);
                }
            } else {
                icon = getSmallIcon(f.getAbsolutePath(), null);
            }
            // 2012.02.28 TOD_ghLim : distinguish between audio type and video type ]
//        }
        return context.getResources().getDrawable(icon);
    }

//    public static Drawable getLargeIconDrawable(File f, Activity context) {
//        int icon;
//        if (f.isDirectory()) {
//            if (Utils.isExternalRootFolder(f.getAbsolutePath()) || Utils.isUSBStorage(f.getAbsolutePath())) {
//                icon = R.drawable.myfile_folder_sdcard_b;
//            } else {
//                icon = R.drawable.myfile_folder_b;
//            }
//        } else {
//            // 2012.02.28 TOD_ghLim : distinguish between audio type and video type [
//            if (needToCheckMimeType(f.getAbsolutePath())) {
//                if (isAudioInMediaStore(f.getAbsolutePath(), context)) {
//                    icon = R.drawable.myfiles_icon_music_default_thumb;
//                } else {
//                    icon = getLargeIcon(f.getAbsolutePath());
//                }
//            } else {
//                icon = getLargeIcon(f.getAbsolutePath());
//            }
//            // 2012.02.28 TOD_ghLim : distinguish between audio type and video type ]
//        }
//        return context.getResources().getDrawable(icon);
//    }

    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = sMimeTypeMap.get(mimeType);
        return (value == null ? 0 : value.intValue());
    }

    public static String getMimeTypeForExtention(String extention) {
        return sMimeType.get(extention);
    }

    public static String getMimeTypeForView(String extention) {
        if (extention == null)
            return null;
        MediaFileType mediaType = sFileTypeMap.get(extention.toUpperCase(Locale.US));

        return (mediaType == null ? null : mediaType.mimeType);
    }    

    public static boolean needToCheckMimeType(String path) {
        String ext = getExtension(path);
        if (ext == null)
            return false;
        if ("MP4".equals(ext) || "3GP".equals(ext) || "3G2".equals(ext) || "ASF".equals(ext)
                || "3GPP".equals(ext)
                || "SCC".equals(ext)
                || "DCF".equals(ext)) {
            return true;
        }
        return false;
    }

    public static String getMimeTypeFromMediaStore(String path, Context context) {
        String ext = getExtension(path);
        if (ext == null)
            return null;
        String mimetype = null;
        try {
            if(SCCFileUtil.isSCCFile(path) ){
                mimetype = SCCFileUtil.getMimetypeFromSCCFile(path);
            } 
            else if (isAudioInMediaStore(path, context)) {
                mimetype = sFileTypeMap.get(ext + "_A").mimeType;
            }
            else {
                mimetype = sFileTypeMap.get(ext).mimeType;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mimetype = null;
        }
        return mimetype;
    }

    public static boolean isAudioInMediaStore( String path, Context context ) {

        //Using Meta data to check if MP4, 3GP, asf files are audio or video files.
        String mimeType = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            if (path != null) {
                retriever.setDataSource(path);
                mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        retriever.release();

        if (mimeType != null) {
            return isAudioFileType(getFileTypeForMimeType(mimeType));
        } else {
            return false;
        }
    }

    public static String getExtension(String path) {
        if (path == null)
            return null;
        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0)
            return null;
        return path.substring(lastDot + 1).toUpperCase(Locale.ENGLISH);
    }

    public static boolean openFile(final File f, Activity context, String attachmentMimeType) {
        
        if(f == null) return false;
        
        String absolutePath = null;
        int mediaType;
        
        try {
            absolutePath = f.getAbsolutePath();
            mediaType = MediaFile.getFileTypeInt(absolutePath);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        
        Intent intent = null;
        boolean result = true;
        String mimeType = attachmentMimeType;
        
        if (mimeType != null && mimeType.equals("application/txt")) {
            mimeType = "text/plain";
        } else if (mimeType == null || "".equals(mimeType)) {
            if (MediaFile.needToCheckMimeType(absolutePath)) {
                mimeType = MediaFile.getMimeTypeFromMediaStore(absolutePath, context);
            } else {
                mimeType = MediaFile.getMimeType(absolutePath);
            }
        }
            
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "";
            // result = false;
        }

        if (MediaFile.isInstallFileType(mediaType)) {
            intent = new Intent(Intent.ACTION_VIEW);

            if (MediaFile.FILE_TYPE_APK == mediaType) {
                intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
            }
            else if (MediaFile.FILE_TYPE_WGT == mediaType) {
                intent.setDataAndType(Uri.fromFile(f), "application/vnd.samsung.widget");
            }
        } else if (MediaFile.isImageFileType(mediaType)) {
            intent = new Intent();

            intent.setAction(android.content.Intent.ACTION_VIEW);
//            Uri myUri = pathToUri(absolutePath, context);
//
//            if (myUri == null)
            Uri myUri = Uri.fromFile(f);
            intent.setDataAndType(myUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, myUri);

            Log.i(TAG, "Clicked Uri => " /* + myUri */+ " MIME type = " + mimeType);
        } else {
            intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(f);
            String ext = getExtension(f.getAbsolutePath());
            try {
                if(SCCFileUtil.isSCCFile(absolutePath) ){
                    try {
                        mimeType = SCCFileUtil.getMimetypeFromSCCFile(absolutePath);
                    } catch(Exception e) {}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if("application/octet-stream".equals(mimeType)) {
                    if (ext != null && "eml".equalsIgnoreCase(ext)) {
                    mimeType="message/rfc822";
                } else if(ext != null && "imy".equalsIgnoreCase(ext)) {
                    mimeType="audio/imelody";
                }else if( ext != null && ("pfx".equalsIgnoreCase(ext) || "p12".equalsIgnoreCase(ext))) {
                    mimeType="application/x-pkcs12";
                }  
                else if (ext != null && "vnt".equalsIgnoreCase(ext)) {
                    mimeType="text/x-vnote";
                }else if(ext != null && "VTS".equalsIgnoreCase(ext)){
                    mimeType = "text/x-vtodo";
                }
            }
            intent.setDataAndType(uri, mimeType);
            
            // if open SNote file for preview, can't edit file at SNote. (same concept with gallery)
            if(mimeType.equals("application/spd"))
            {
                intent.putExtra("isFromEmailPreview", true);
            }
            // if open SNote file for preview, can't edit file at SNote. (same concept with gallery)
            
            //SNMC_mime_CHANDU_VNOTE_CHANGE_START
            if(mimeType.equals("text/x-vnote")){            	
                intent.putExtra("isSavedMemo", true);
            }
            //SNMC_mime_CHANDU_VNOTE_CHANGE_END
            //if (!(ext != null && "VTS".equalsIgnoreCase(ext)))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (DEBUG)
                Log.w(TAG, "Clicked uri : mime type = " + mimeType);
        }
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                result = false;
        }

        return result;
    }

    public static String uriToPath(Context context, Uri uri) {
        if (DEBUG)
            Log.w(TAG, "URI = "+uri);
        if (uri == null || context == null) {
            return null;
        }

        String data = null;
        String scheme = uri.getScheme();
        if (DEBUG)
            Log.w(TAG, "SCHEME = "+scheme);
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (index > -1) {
                    data = cursor.getString(index);
                }
                cursor.close();
            }
        }

        if (DEBUG)
            Log.w(TAG, "DATA = "+data);
        return data;
    }

    public static Uri pathToUri(Activity context, String path) {
        if (DEBUG)
            Log.w(TAG, "PATH = "+path);
        Uri uri = null;
        File f = new File(path);
        if (!f.exists()) {
            if (DEBUG)
                Log.w("TAG", "Uri :  is not existed");
            return null;
        }

        try {
            ContentResolver cr = context.getContentResolver();
            long rowId = 0;
            String mimetype;
            mimetype = MediaFile.getShareMimeType(path, context);
            int mediaType = MediaFile.getFileTypeForMimeType(mimetype);
            // int mediaType = MediaFile.getFileTypeInt(f.getName());
            boolean isImage = MediaFile.isImageFileType(mediaType);
            boolean isVideo = MediaFile.isVideoFileType(mediaType);
            boolean isAudio = MediaFile.isAudioFileType(mediaType);
            if (isImage) {
                Cursor c = cr.query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, MediaColumns.DATA + "= ? COLLATE LOCALIZED", new String[] {
                            path,
                        }, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        rowId = c.getLong(c.getColumnIndex(MediaColumns._ID));
                        uri = Uri.parse("content://media/external/images/media/" + rowId);
                    }
                    c.close();
                } else {
                    if (DEBUG)
                        Log.w(TAG, "FileUtils : c is null");
                }
            } else if (isVideo) {
                Cursor c = cr.query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null, MediaColumns.DATA + "= ? COLLATE LOCALIZED", new String[] {
                            path,
                        }, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        rowId = c.getLong(c.getColumnIndex(MediaColumns._ID));
                        uri = Uri.parse("content://media/external/video/media/" + rowId);
                    }
                    c.close();
                } else {
                    if (DEBUG)
                        Log.w(TAG, "FileUtils : c is null");
                }
            } else if (isAudio) {
                Cursor c = cr.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, MediaColumns.DATA + "= ? COLLATE LOCALIZED", new String[] {
                            path,
                        }, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        rowId = c.getLong(c.getColumnIndex(MediaColumns._ID));
                        uri = Uri.parse("content://media/external/audio/media/" + rowId);
                    }
                    c.close();
                } else {
                    if (DEBUG)
                        Log.w(TAG, "FileUtils : c is null");
                }
            } else {
                uri = Uri.fromFile(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (uri == null) {
            if (DEBUG)
                Log.w("TAG", "URI is null");
            uri = Uri.fromFile(f);
        }
        if (DEBUG)
            Log.w("TAG", "Uri : ");
        return uri;
    }
    
    public static class SCCFileUtil {

        public static String getMimetypeFromSCCFile(String path) throws IOException {
            File sccFile = new File(path);
            FileInputStream fis = new FileInputStream(sccFile);
            byte[] pklenBuf = new byte[4];
            byte[] mimetypeBuf = new byte[128];

            try {
                long skip = fis.skip(22);
                int read = fis.read(pklenBuf, 0, 4);
                int pklen = byteToInt(pklenBuf, ByteOrder.LITTLE_ENDIAN);
                skip = fis.skip(12);
                read = fis.read(mimetypeBuf, 0, pklen);
                String result = new String(mimetypeBuf);
                fis.close();
                if (result.contains("application/vnd.samsung.scc") == false) {
                    return "ERR_NOT_SCC_FILE";
                }
                return result.trim();
            } catch (Exception e) {
                fis.close();
                return e.toString();
            }
        }

        public static boolean isSCCFile(String path) throws IOException {
            File sccFile = new File(path);
            FileInputStream fis = null; // new FileInputStream(sccFile);
            byte[] pklenBuf = new byte[4];
            byte[] mimetypeBuf = new byte[128];

            try {
                fis = new FileInputStream(sccFile);

                long skip = fis.skip(22);
                int read = fis.read(pklenBuf, 0, 4);
                int pklen = byteToInt(pklenBuf, ByteOrder.LITTLE_ENDIAN);
                skip = fis.skip(12);
                read = fis.read(mimetypeBuf, 0, pklen);
                String result = new String(mimetypeBuf);
                fis.close();
                if (result.contains("application/vnd.samsung.scc"))
                    return true;
                else
                    return false;
            } catch (Exception e) {
                if (fis != null)
                    fis.close();
                return false;
            }
        }

        private static int byteToInt(byte[] bytes, ByteOrder order) {
            ByteBuffer buff = ByteBuffer.allocate(4);
            buff.order(order);
            buff.put(bytes);
            buff.flip();
            return buff.getInt();
        }
        
        public static int getSmallIcon( String path ) {
            if (!TextUtils.isEmpty(path)) {
                try {
                    String mimetype = getMimetypeFromSCCFile(path);
                    if (mimetype.equals("application/vnd.samsung.scc.storyalbum")) {
                        return R.drawable.mime_detail_ic_story_album;
                    } else if (mimetype.equals("application/vnd.samsung.scc.pinall")) {
                        return R.drawable.mime_detail_ic_scrap_book;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return R.drawable.mime_detail_ic_scc;
        }
        
        
        public static int getLargeIcon(String path) {
            if (!TextUtils.isEmpty(path)) {
                try {
                    String mimetype = getMimetypeFromSCCFile(path);
                    if (mimetype.equals("application/vnd.samsung.scc.storyalbum")) {
                        return R.drawable.mime_detail_ic_story_album;
                    } else if (mimetype.equals("application/vnd.samsung.scc.pinall")) {
                        return R.drawable.mime_detail_ic_scrap_book;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return R.drawable.mime_detail_ic_scc;
        }
    }
    
    // => /data/data/com.example.apk-2/
    public static String getCurrentFilePath(Context context) {
        String dir = context.getFilesDir().getAbsolutePath();
        Log.d(TAG, "getCurrentDataPath(), work dir="+dir);
        return dir;
    }
    
    // => /data/app/com.example.apk-2/
    public static String getCurrentApkPath(Context context) {
        String dir = context.getPackageResourcePath();
        Log.d(TAG, "getCurrentApkPath(), installed dir="+dir);
        return dir;
    }
    
    // => /data/data/com.example.apk-2/
    public static String getCurrentSDCardCachePath(Context context) {
        String dir = context.getExternalCacheDir().getAbsolutePath();
        Log.d(TAG, "getCurrentSDCardCachePath(), SDCard cache dir="+dir);
        return dir;
    }
    
    // => database dir
    public static String getCurrentDBPath(Context context, String db) {
        String dir = context.getDatabasePath(db).getAbsolutePath();
        Log.d(TAG, "getCurrentDBPath(), DB of \""+db+"\" dir="+dir);
        return dir;
    }
    
    public static boolean isSdcardAvailable() {
        String state = Environment.getExternalStorageState();
        Log.i(TAG, "ExternalStorageState: "+state);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}

