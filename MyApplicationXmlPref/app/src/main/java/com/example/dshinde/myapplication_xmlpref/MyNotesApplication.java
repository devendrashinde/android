
package com.example.dshinde.myapplication_xmlpref;

import static org.acra.ReportingInteractionMode.DIALOG;
import static org.acra.ReportingInteractionMode.NOTIFICATION;
import static org.acra.ReportingInteractionMode.SILENT;
import static org.acra.ReportingInteractionMode.TOAST;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;
import org.acra.sender.HttpSender;

public class MyNotesApplication extends Application {
    private String userId;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        /*
        final ACRAConfiguration config;
        try {
            config = new ConfigurationBuilder(this)
                    .setMailTo("geetds@googlemail.com")
                    .setReportType(HttpSender.Type.JSON)
                    .setSendReportsInDevMode(true)
                    .setReportField(ReportField.STACK_TRACE, true)
                    .setReportField(ReportField.PACKAGE_NAME, true)
                    .setReportingInteractionMode(SILENT)
                    .build();
            // Initialise ACRA
            ACRA.init(this, config);
        } catch (ACRAConfigurationException e) {
            e.printStackTrace();
        }
         */
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

}
