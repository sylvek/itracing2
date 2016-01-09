/* Copyright (c) 2012 -- CommonsWare, LLC

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.sylvek.itracing2.database;

import android.content.Loader;
import android.os.AsyncTask;

public abstract class ContentChangingTask extends
        AsyncTask<Object, Void, Void> {
    private Loader<?> loader = null;

    public ContentChangingTask(Loader<?> loader)
    {
        this.loader = loader;
    }

    @Override
    protected void onPostExecute(Void param)
    {
        loader.onContentChanged();
    }
}
