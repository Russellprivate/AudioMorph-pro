package com.example.localization

import com.example.model.AppLanguage

object StringsLocalization {

    private val en = mapOf(
        "app_title" to "AudioMorph",
        "app_tagline" to "Fast Video to Audio Converter & Studio",
        "tab_convert" to "Convert",
        "tab_studio" to "Studio / Trim",
        "tab_library" to "Library",
        "tab_cloud" to "Cloud Sync",
        "tab_settings" to "Settings",

        // Convert Screen
        "select_video" to "Select Video",
        "select_video_desc" to "Choose any video from gallery or file manager",
        "demo_video" to "Use Demo Video",
        "demo_video_desc" to "Test converter instantly with sample video",
        "video_selected" to "Video Selected",
        "duration" to "Duration",
        "file_size" to "File Size",
        "resolution" to "Resolution",
        "export_settings" to "Export Settings",
        "audio_format" to "Audio Format",
        "audio_quality" to "Audio Quality (Bitrate)",
        "metadata_optional" to "Metadata Tags (Optional)",
        "track_title" to "Track Title",
        "artist_name" to "Artist Name",
        "album_name" to "Album Name",
        "convert_button" to "Convert to Audio",
        "converting" to "Converting Video...",
        "fast_export_active" to "Ultra-Fast Direct Stream Extraction",
        "conversion_success" to "Conversion Complete!",
        "open_in_studio" to "Open in Trimming Studio",
        "play_now" to "Play Now",
        "view_in_library" to "View in Library",

        // Studio Screen
        "trim_title" to "Audio Trimmer & Editor",
        "select_audio_to_edit" to "Select Audio to Edit",
        "trim_range" to "Trim Selection",
        "start_time" to "Start Time",
        "end_time" to "End Time",
        "play_selection" to "Play Selection",
        "pause" to "Pause",
        "fade_in" to "Fade In",
        "fade_out" to "Fade Out",
        "volume_boost" to "Volume Boost",
        "speed" to "Playback Speed",
        "save_cut" to "Export Cut Audio",
        "save_ringtone" to "Set as Ringtone / Alert",
        "processing_edit" to "Applying Audio Effects...",
        "edit_complete" to "Audio Trimmed Successfully!",

        // Library Screen
        "library_title" to "Audio Library",
        "search_audio" to "Search converted files...",
        "all_formats" to "All",
        "empty_library" to "No audio files yet",
        "empty_library_desc" to "Convert your first video to start building your collection",
        "play" to "Play",
        "share" to "Share",
        "delete" to "Delete",
        "details" to "Details",
        "lock_private" to "Lock in Vault",
        "unlock_private" to "Move to Public",
        "synced" to "Synced",
        "syncing" to "Syncing...",
        "sync_pending" to "Pending Sync",
        "local_only" to "Offline Only",

        // Cloud Sync Screen
        "cloud_sync_title" to "Cloud Storage Auto-Sync",
        "cloud_sync_desc" to "Automatically backup your converted audio files to the cloud",
        "auto_sync_toggle" to "Enable Auto-Sync",
        "wifi_only_toggle" to "Sync only over Wi-Fi",
        "cloud_provider" to "Cloud Destination",
        "sync_now" to "Sync All Now",
        "sync_status_summary" to "Cloud Queue Status",
        "synced_count" to "Files Synced",
        "pending_count" to "Pending Upload",
        "last_sync" to "Last Synced",
        "never_synced" to "Never",

        // Settings Screen
        "settings_title" to "Settings & Security",
        "app_language" to "App Language",
        "appearance" to "Appearance & Theme",
        "theme_system" to "System Default",
        "theme_light" to "Light Mode",
        "theme_dark" to "Dark Mode",
        "theme_amoled" to "AMOLED Pure Black",
        "security" to "Security & App Lock",
        "biometric_lock" to "Biometric / PIN Lock",
        "biometric_desc" to "Protect converted files with fingerprint or PIN",
        "set_pin" to "Set 4-Digit PIN",
        "change_pin" to "Change PIN",
        "offline_ready" to "Offline Processing Engine",
        "offline_ready_desc" to "All video conversion and trimming runs 100% locally on your device without internet.",
        "default_format" to "Default Audio Format",
        "default_bitrate" to "Default Quality",
        "about_app" to "About AudioMorph",
        "version" to "Version 1.0 (High Performance)",

        // Security / Lock Screen
        "enter_pin" to "Enter Security PIN",
        "unlock_with_biometrics" to "Unlock with Biometrics",
        "biometric_prompt_title" to "Unlock AudioMorph",
        "biometric_prompt_subtitle" to "Use fingerprint or face recognition to continue",
        "wrong_pin" to "Incorrect PIN, please try again",
        "unlock" to "Unlock"
    )

    private val bn = mapOf(
        "app_title" to "AudioMorph",
        "app_tagline" to "ভিডিও থেকে দ্রুত অডিও কনভার্টার ও স্টুডিও",
        "tab_convert" to "কনভার্ট",
        "tab_studio" to "অডিও স্টুডিও",
        "tab_library" to "লাইব্রেরি",
        "tab_cloud" to "ক্লাউড সিঙ্ক",
        "tab_settings" to "সেটিংস",

        // Convert Screen
        "select_video" to "ভিডিও নির্বাচন করুন",
        "select_video_desc" to "গ্যালারি বা ফাইল ম্যানেজার থেকে যেকোনো ভিডিও বেছে নিন",
        "demo_video" to "ডেমো ভিডিও ব্যবহার করুন",
        "demo_video_desc" to "নমুনা ভিডিও দিয়ে দ্রুত কনভার্টার পরীক্ষা করুন",
        "video_selected" to "ভিডিও নির্বাচিত হয়েছে",
        "duration" to "সময়কাল",
        "file_size" to "ফাইলের আকার",
        "resolution" to "রেজোলিউশন",
        "export_settings" to "এক্সপোর্ট সেটিংস",
        "audio_format" to "অডিও ফরম্যাট",
        "audio_quality" to "অডিও কোয়ালিটি (বিটরেট)",
        "metadata_optional" to "মেটাডাটা ট্যাগ (ঐচ্ছিক)",
        "track_title" to "গানের নাম / শিরোনাম",
        "artist_name" to "শিল্পীর নাম",
        "album_name" to "অ্যালবামের নাম",
        "convert_button" to "অডিওতে কনভার্ট করুন",
        "converting" to "ভিডিও কনভার্ট হচ্ছে...",
        "fast_export_active" to "অতি-দ্রুত ডাইরেক্ট স্ট্রিম এক্সট্রাকশন",
        "conversion_success" to "কনভার্ট সম্পন্ন হয়েছে!",
        "open_in_studio" to "অডিও কাটতে স্টুডিওতে যান",
        "play_now" to "এখনই শুনুন",
        "view_in_library" to "লাইব্রেরিতে দেখুন",

        // Studio Screen
        "trim_title" to "অডিও কাটিং ও এডিটিং",
        "select_audio_to_edit" to "এডিট করার অডিও নির্বাচন করুন",
        "trim_range" to "অডিও কাটার পরিসর",
        "start_time" to "শুরুর সময়",
        "end_time" to "শেষের সময়",
        "play_selection" to "কাটা অংশ শুনুন",
        "pause" to "বিরতি",
        "fade_in" to "ফেড-ইন (Fade In)",
        "fade_out" to "ফেড-আউট (Fade Out)",
        "volume_boost" to "ভলিউম বুস্ট",
        "speed" to "প্লেব্যাক স্পিড",
        "save_cut" to "কাটা অডিও সেভ করুন",
        "save_ringtone" to "রিংটোন হিসেবে সেট করুন",
        "processing_edit" to "অডিও ইফেক্ট প্রয়োগ হচ্ছে...",
        "edit_complete" to "অডিও সফলভাবে কাটা হয়েছে!",

        // Library Screen
        "library_title" to "অডিও লাইব্রেরি",
        "search_audio" to "ফাইল খুঁজুন...",
        "all_formats" to "সব ফরম্যাট",
        "empty_library" to "এখনো কোনো অডিও ফাইল নেই",
        "empty_library_desc" to "ভিডিও কনভার্ট করে আপনার অডিও সংগ্রহ শুরু করুন",
        "play" to "প্লে করুন",
        "share" to "শেয়ার করুন",
        "delete" to "মুছুন",
        "details" to "বিবরণ",
        "lock_private" to "লক করুন (ভল্ট)",
        "unlock_private" to "লক মুক্ত করুন",
        "synced" to "সিঙ্ক হয়েছে",
        "syncing" to "সিঙ্ক হচ্ছে...",
        "sync_pending" to "সিঙ্ক বাকি আছে",
        "local_only" to "শুধুমাত্র অফলাইন",

        // Cloud Sync Screen
        "cloud_sync_title" to "ক্লাউড স্টোরেজ অটো-সিঙ্ক",
        "cloud_sync_desc" to "কনভার্ট করা অডিও স্বয়ংক্রিয়ভাবে ক্লাউডে ব্যাকআপ রাখুন",
        "auto_sync_toggle" to "অটো-সিঙ্ক চালু করুন",
        "wifi_only_toggle" to "শুধুমাত্র ওয়াই-ফাই ব্যবহার করুন",
        "cloud_provider" to "ক্লাউড স্টোরেজ প্রদানকারী",
        "sync_now" to "সব ফাইল এখনই সিঙ্ক করুন",
        "sync_status_summary" to "ক্লাউড কিউ স্ট্যাটাস",
        "synced_count" to "সিঙ্ক সম্পন্ন ফাইল",
        "pending_count" to "আপলোডের অপেক্ষায়",
        "last_sync" to "সর্বশেষ সিঙ্ক",
        "never_synced" to "কখনও নয়",

        // Settings Screen
        "settings_title" to "সেটিংস ও নিরাপত্তা",
        "app_language" to "অ্যাপের ভাষা",
        "appearance" to "থিম ও ডিসপ্লে",
        "theme_system" to "সিস্টেম ডিফল্ট",
        "theme_light" to "লাইট মোড",
        "theme_dark" to "ডার্ক মোড",
        "theme_amoled" to "অ্যামোলেড পিউর ব্ল্যাক",
        "security" to "নিরাপত্তা ও অ্যাপ লক",
        "biometric_lock" to "বায়োমেট্রিক / পিন লক",
        "biometric_desc" to "ফিঙ্গারপ্রিন্ট বা পিন দিয়ে ফাইল সুরক্ষিত রাখুন",
        "set_pin" to "৪ সংখ্যার পিন সেট করুন",
        "change_pin" to "পিন পরিবর্তন করুন",
        "offline_ready" to "১০০% অফলাইন ইঞ্জিন",
        "offline_ready_desc" to "সব ভিডিও কনভার্সন এবং এডিটিং আপনার ডিভাইসেই স্থানীয়ভাবে কোনো ইন্টারনেট ছাড়াই সম্পন্ন হয়।",
        "default_format" to "ডিফল্ট অডিও ফরম্যাট",
        "default_bitrate" to "ডিফল্ট কোয়ালিটি",
        "about_app" to "AudioMorph সম্পর্কে",
        "version" to "ভার্সন ১.০ (উচ্চ গতিসম্পন্ন)",

        // Security / Lock Screen
        "enter_pin" to "সিকিউরিটি পিন দিন",
        "unlock_with_biometrics" to "বায়োমেট্রিক দিয়ে আনলক করুন",
        "biometric_prompt_title" to "AudioMorph আনলক করুন",
        "biometric_prompt_subtitle" to "ফিঙ্গারপ্রিন্ট বা ফেস লক ব্যবহার করুন",
        "wrong_pin" to "ভুল পিন, আবার চেষ্টা করুন",
        "unlock" to "আনলক"
    )

    private val es = mapOf(
        "app_title" to "AudioMorph",
        "app_tagline" to "Conversor de video a audio rápido y estudio",
        "tab_convert" to "Convertir",
        "tab_studio" to "Estudio / Cortar",
        "tab_library" to "Biblioteca",
        "tab_cloud" to "Nube",
        "tab_settings" to "Ajustes",
        "select_video" to "Seleccionar video",
        "convert_button" to "Convertir a audio",
        "converting" to "Convirtiendo...",
        "trim_title" to "Cortador y editor de audio",
        "library_title" to "Biblioteca de audio",
        "cloud_sync_title" to "Sincronización en la nube",
        "settings_title" to "Ajustes y seguridad",
        "biometric_lock" to "Bloqueo biométrico / PIN",
        "enter_pin" to "Ingresar PIN de seguridad"
    )

    private val hi = mapOf(
        "app_title" to "AudioMorph",
        "app_tagline" to "फास्ट वीडियो से ऑडियो कनवर्टर और स्टूडियो",
        "tab_convert" to "कन्वर्ट",
        "tab_studio" to "स्टूडियो",
        "tab_library" to "लाइब्रेरी",
        "tab_cloud" to "क्लाउड सिंक",
        "tab_settings" to "सेटिंग्स",
        "select_video" to "वीडियो चुनें",
        "convert_button" to "ऑडियो में बदलें",
        "converting" to "कन्वर्ट हो रहा है...",
        "trim_title" to "ऑडियो ट्रिमर और एडिटर",
        "library_title" to "ऑडियो लाइब्रेरी",
        "cloud_sync_title" to "क्लाउड स्टोरेज सिंक",
        "settings_title" to "सेटिंग्स और सुरक्षा",
        "biometric_lock" to "बायोमेट्रिक / पिन लॉक",
        "enter_pin" to "सुरक्षा पिन दर्ज करें"
    )

    fun get(key: String, language: AppLanguage = AppLanguage.ENGLISH): String {
        return when (language) {
            AppLanguage.BENGALI -> bn[key] ?: en[key] ?: key
            AppLanguage.SPANISH -> es[key] ?: en[key] ?: key
            AppLanguage.HINDI -> hi[key] ?: en[key] ?: key
            AppLanguage.ENGLISH -> en[key] ?: key
        }
    }
}
