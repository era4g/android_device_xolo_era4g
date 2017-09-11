/*
 * Copyright (C) 2012-2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony;

import static com.android.internal.telephony.RILConstants.*;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.telephony.SmsMessage;
import android.os.SystemProperties;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.telephony.Rlog;

import android.telephony.SignalStrength;

import android.telephony.PhoneNumberUtils;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
import com.android.internal.telephony.cdma.SignalToneUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;

public class SamsungGtexslteRIL extends SamsungSPRDRIL implements CommandsInterface {

    public static final int RIL_UNSOL_SIM_PB_READY = 11021;
    public static final int RIL_UNSOL_GPS_NOTI = 11009;
    public static final int RIL_UNSOL_AM = 11010;
    public static final int RIL_UNSOL_STK_CALL_CONTROL_RESULT = 11003;

    public SamsungGtexslteRIL(Context context, int preferredNetworkType,
            int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        mQANElements = SystemProperties.getInt("ro.ril.telephony.mqanelements", 4);
    }

    public SamsungGtexslteRIL(Context context, int networkMode,
            int cdmaSubscription) {
        super(context, networkMode, cdmaSubscription);
        mQANElements = SystemProperties.getInt("ro.ril.telephony.mqanelements", 4);
    }

    @Override
    protected void
    processUnsolicited (Parcel p) {
        Object ret;
        int dataPosition = p.dataPosition(); // save off position within the Parcel
        int response = p.readInt();
        Rlog.e(RILJ_LOG_TAG, "ProcessUnsolicited: " + response);
        switch(response) {
            case 11008: // RIL_UNSOL_DEVICE_READY_NOTI
                ret = responseVoid(p); // Currently we'll bypass logcat for this first
                break;
            // SAMSUNG STATES
            case RIL_UNSOL_GPS_NOTI:
                ret = responseVoid(p);
                break;
            case RIL_UNSOL_AM: // RIL_UNSOL_AM:
                ret = responseString(p);
                String amString = (String) ret;
                Rlog.d(RILJ_LOG_TAG, "Executing AM: " + amString);

                try {
                    Runtime.getRuntime().exec("am " + amString);
                } catch (IOException e) {
                    e.printStackTrace();
                    Rlog.e(RILJ_LOG_TAG, "am " + amString + " could not be executed.");
                }
                break;
            case RIL_UNSOL_SIM_PB_READY: // RIL_UNSOL_RESPONSE_HANDOVER:
                ret = responseVoid(p);
                break;
            case RIL_UNSOL_STK_CALL_CONTROL_RESULT:
                ret = responseVoid(p);
                break;
            case 20012:
                ret = responseVoid(p);
                break;
            default:
                // Rewind the Parcel
                p.setDataPosition(dataPosition);

                // Forward responses that we are not overriding to the super class
                super.processUnsolicited(p);
                return;
        }

    }

    @Override
    public void
    acceptCall (Message result) {
       RILRequest rr
               = RILRequest.obtain(RIL_REQUEST_ANSWER, result);

       rr.mParcel.writeInt(1);
       rr.mParcel.writeInt(0);

       if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));

       send(rr);
    }
}
