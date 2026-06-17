package com.example.upad.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class LastTaskWidget : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget =
        LastTaskWidgetContent()
}