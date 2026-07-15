package com.example.ui.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast

object MediaUtils {
    fun downloadMediaFile(context: Context, url: String, title: String, mimeType: String) {
        try {
            if (url.isEmpty()) {
                Toast.makeText(context, "No URL provided to download.", Toast.LENGTH_SHORT).show()
                return
            }
            if (!url.startsWith("http")) {
                Toast.makeText(context, "Local or preview asset cannot be downloaded via URL.", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = Uri.parse(url)
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri).apply {
                setTitle("Downloading $title")
                setDescription("Saving media from Nexus AI")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Nexus_${System.currentTimeMillis()}_$title")
                setMimeType(mimeType)
            }
            downloadManager.enqueue(request)
            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareMedia(context: Context, title: String, url: String) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out this AI creation '$title' generated with Nexus AI:\n$url")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share AI Creation"))
        } catch (e: Exception) {
            Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun copyToClipboard(context: Context, text: String, label: String = "AI Response") {
        try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, text: String) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Response"))
        } catch (e: Exception) {
            Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show()
        }
    }
}
