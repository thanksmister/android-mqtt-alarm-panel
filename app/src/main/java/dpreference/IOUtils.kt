/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dpreference


import android.database.Cursor

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader

internal object IOUtils {

    // NOTE: This class is focussed on InputStream, OutputStream, Reader and
    // Writer. Each method should take at least one of these as a parameter,
    // or return one of them.

    private val EOF = -1

    /**
     * The default buffer size ({@value}) to use for [ ][.copyLarge] and []
     */
    private val DEFAULT_BUFFER_SIZE = 1024

    fun closeQuietly(`is`: InputStream?) {
        if (`is` != null) {
            try {
                `is`.close()
            } catch (e: IOException) {
                // ignore
            }

        }
    }

    fun closeQuietly(os: OutputStream?) {
        if (os != null) {
            try {
                os.close()
            } catch (e: IOException) {
                // ignore
            }

        }
    }

    fun closeQuietly(r: Reader?) {
        if (r != null) {
            try {
                r.close()
            } catch (e: IOException) {
                // ignore
            }

        }
    }

    fun closeQuietly(cursor: Cursor?) {
        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
    }


}
