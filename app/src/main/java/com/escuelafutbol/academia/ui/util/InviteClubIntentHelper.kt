package com.escuelafutbol.academia.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.escuelafutbol.academia.R

object InviteClubIntentHelper {

    enum class InviteTarget {
        COACH,
        COORDINATOR,
        PARENT,
    }

    fun inviteBodyForTarget(context: Context, academyName: String, code: String, target: InviteTarget): String =
        when (target) {
            InviteTarget.COACH ->
                context.getString(R.string.invite_message_coach, academyName, code)
            InviteTarget.COORDINATOR ->
                context.getString(R.string.invite_message_coordinator, academyName, code)
            InviteTarget.PARENT ->
                context.getString(R.string.invite_message_parent, academyName, code)
        }

    fun inviteBody(context: Context, academyName: String, code: String): String =
        context.getString(R.string.invite_club_full_message, academyName, code)

    fun shareInviteText(context: Context, academyName: String, code: String) {
        val text = inviteBody(context, academyName, code)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(
            send,
            context.getString(R.string.invite_share_chooser_title),
        )
        runCatching { context.startActivity(chooser) }
    }

    fun shareInviteTextForTarget(
        context: Context,
        academyName: String,
        code: String,
        target: InviteTarget,
    ) {
        val text = inviteBodyForTarget(context, academyName, code, target)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(
            send,
            context.getString(R.string.invite_share_chooser_title),
        )
        runCatching { context.startActivity(chooser) }
    }

    fun copyCode(context: Context, code: String): Boolean {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return false
        cm.setPrimaryClip(ClipData.newPlainText("club_code", code))
        Toast.makeText(context, R.string.academy_club_code_copied, Toast.LENGTH_SHORT).show()
        return true
    }

    fun openEmailDraftForTarget(
        context: Context,
        academyName: String,
        code: String,
        target: InviteTarget,
    ) {
        val subject = context.getString(R.string.invite_email_subject, academyName)
        val body = inviteBodyForTarget(context, academyName, code, target)
        val uri = Uri.parse(
            "mailto:?${encodeMailQuery(subject, body)}",
        )
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        runCatching { context.startActivity(intent) }
    }

    fun openEmailDraft(context: Context, academyName: String, code: String) {
        val subject = context.getString(R.string.invite_email_subject, academyName)
        val body = inviteBody(context, academyName, code)
        val uri = Uri.parse(
            "mailto:?${encodeMailQuery(subject, body)}",
        )
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        runCatching { context.startActivity(intent) }
    }

    private fun encodeMailQuery(subject: String, body: String): String =
        "subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
}
