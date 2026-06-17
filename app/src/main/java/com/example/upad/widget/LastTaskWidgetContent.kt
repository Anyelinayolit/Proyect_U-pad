package com.example.upad.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.upad.R
import androidx.glance.background
import androidx.glance.color.ColorProvider

class LastTaskWidgetContent : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        provideContent {

            LastTaskCard(context)

        }
    }
}

@Composable
private fun LastTaskCard(
    context: Context
) {

    val prefs =
        context.getSharedPreferences(
            "WIDGET_PREFS",
            Context.MODE_PRIVATE
        )

    val ultimaTarea =
        prefs.getString(
            "ULTIMA_TAREA",
            "Sin actividad"
        ) ?: "Sin actividad"

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ImageProvider(
                    R.drawable.widget_last_task_bg
                )
            )
            .padding(16.dp)
    ) {

        Image(
            provider = ImageProvider(R.drawable.child_avatar),
            contentDescription = "Niño",
            modifier = GlanceModifier.size(100.dp)
        )

        Spacer(
            modifier = GlanceModifier.width(16.dp)
        )

        Column {

            Text(
                text = "La última tarea que hizo fue:"
            )

            Spacer(
                modifier = GlanceModifier.height(12.dp)
            )

            Text(
                text = ultimaTarea
            )
        }
    }
}