package com.sevtinge.cemiuiler.module;

import com.sevtinge.cemiuiler.module.base.BaseModule;
import com.sevtinge.cemiuiler.module.guardprovider.DisableUploadAppList;

public class GuardProvider extends BaseModule {
    @Override
    public void handleLoadPackage() {
        initHook(new DisableUploadAppList(), mPrefsMap.getBoolean("disable_upload_applist"));
    }
}
