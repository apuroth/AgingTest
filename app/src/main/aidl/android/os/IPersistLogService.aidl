// IPersistLogService.aidl
package android.os;

import android.os.IPersistLogCallback;
// Declare any non-default types here with import statements

/** @hide */
interface IPersistLogService {
    boolean write(String fileName, String data);
    void read(String fileName, IPersistLogCallback cb);
}
