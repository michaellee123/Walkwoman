/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package dog.abcd.walkwoman.notification

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.constant.Constant.ACTION_QUIT
import dog.abcd.walkwoman.constant.Constant.ACTION_REWIND
import dog.abcd.walkwoman.constant.Constant.ACTION_SKIP
import dog.abcd.walkwoman.constant.Constant.ACTION_TOGGLE_PAUSE
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.services.PlaybackService
import dog.abcd.walkwoman.view.activity.MainActivity

@SuppressLint("RestrictedApi")
class PlayingNotificationImpl24(
    val context: PlaybackService,
    val mediaSessionToken: MediaSessionCompat.Token
) : PlayingNotification(context) {

    init {
        val action = Intent(context, MainActivity::class.java)
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val clickIntent =
            PendingIntent.getActivity(
                context,
                0,
                action,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val serviceName = ComponentName(context, PlaybackService::class.java)
        val intent = Intent(ACTION_QUIT)
        intent.component = serviceName
        val deleteIntent = PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseAction = buildPlayAction(true)
        val previousAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous,
            context.getString(R.string.action_previous),
            retrievePlaybackAction(ACTION_REWIND)
        )
        val nextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next,
            context.getString(R.string.action_next),
            retrievePlaybackAction(ACTION_SKIP)
        )

        setSmallIcon(R.mipmap.ic_launcher_foreground)
        setContentIntent(clickIntent)
        setDeleteIntent(deleteIntent)
        setShowWhen(false)
        addAction(previousAction)
        addAction(playPauseAction)
        addAction(nextAction)

        setStyle(
            MediaStyle()
                .setMediaSession(mediaSessionToken)
                .setShowActionsInCompactView(0, 1, 2)
        )
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    override fun updateMetadata(song: Song, onUpdate: () -> Unit) {
        setContentTitle(song.title)
        setContentText(song.artist)
        setSubText(song.bucketDisplayName)

        setSmallIcon(if (song.isHiRes) R.mipmap.hires_notification else R.mipmap.ic_walkman)
        Glide.with(context)
            .asBitmap()
            .load(song.albumArt)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    setLargeIcon(resource)
                    onUpdate()
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    setLargeIcon(
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.mipmap.default_audio_art
                        )
                    )
                    onUpdate()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    setLargeIcon(
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.mipmap.default_audio_art
                        )
                    )
                    onUpdate()
                }
            })
    }

    private fun buildPlayAction(isPlaying: Boolean): NotificationCompat.Action {
        val playButtonResId =
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        return NotificationCompat.Action.Builder(
            playButtonResId,
            context.getString(R.string.action_play_pause),
            retrievePlaybackAction(ACTION_TOGGLE_PAUSE)
        ).build()
    }


    override fun setPlaying(isPlaying: Boolean) {
        mActions[1] = buildPlayAction(isPlaying)
    }

    private fun retrievePlaybackAction(action: String): PendingIntent {
        val serviceName = ComponentName(context, PlaybackService::class.java)
        val intent = Intent(action)
        intent.component = serviceName
        return PendingIntent.getService(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {

        fun from(
            context: PlaybackService,
            notificationManager: NotificationManager,
            sessionToken: MediaSessionCompat.Token
        ): PlayingNotification {
            createNotificationChannel(context, notificationManager)
            return PlayingNotificationImpl24(context, sessionToken)
        }
    }
}