package com.notjuststudio.netbuffet;

import com.notjuststudio.threadsauce.LockBoolean;

class SecureBase {
    final LockBoolean cryptoProtective = new LockBoolean(false);
}
