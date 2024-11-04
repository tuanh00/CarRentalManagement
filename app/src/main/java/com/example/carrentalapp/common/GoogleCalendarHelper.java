package com.example.carrentalapp.common;

import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

public class GoogleCalendarHelper {
    private Calendar calendarService;

    public GoogleCalendarHelper(GoogleAccountCredential credential) {
        this.calendarService = new Calendar.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("CarRentalApp")
                .build();
    }

    public String createEvent(String title, Date startDate, Date endDate) throws IOException {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Event title must not be empty");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and End dates must not be null");
        }

        Event event = new Event()
                .setSummary(title)
                .setStart(new EventDateTime()
                        .setDateTime(new com.google.api.client.util.DateTime(startDate))
                        .setTimeZone(TimeZone.getDefault().getID()))
                .setEnd(new EventDateTime()
                        .setDateTime(new com.google.api.client.util.DateTime(endDate))
                        .setTimeZone(TimeZone.getDefault().getID()));

        Event createdEvent = calendarService.events().insert("primary", event).execute();
        Log.d("GoogleCalendarHelper", "Event created with ID: " + createdEvent.getId());
        return createdEvent.getId();
    }
}
