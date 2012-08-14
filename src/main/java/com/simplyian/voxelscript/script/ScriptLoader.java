/*
 * This file is part of ScriptEngine.
 *
 * Copyright (c) 2012-2012, THEDevTeam <http://thedevteam.org/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.simplyian.voxelscript.script;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.simplyian.voxelscript.VoxelScriptPlugin;

public class ScriptLoader {
	private final VoxelScriptPlugin plugin;
	private File baseDir;

	public ScriptLoader(VoxelScriptPlugin plugin) {
		this.plugin = plugin;
	}

	public Script loadScript(Context cx, Scriptable scope, String name, File file) {
		int ch;
		StringBuffer strContent = new StringBuffer("");
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
			while ((ch = fin.read()) != -1) {
				strContent.append((char) ch);
			}
			fin.close();
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not read script " + file.getName() + "!", ex);
		}

		String script = strContent.toString();
		return loadScript(cx, scope, name, script);
	}

	/**
	 * Loads the given script.
	 * 
	 * @param cx
	 * @param scope
	 *            The scope to load the script into.
	 * @param name
	 * @param script
	 * @return The loaded script.
	 */
	private Script loadScript(Context cx, Scriptable scope, String name, String script) {
		try {
			// Setup some variables to get the result
			ScriptableObject.putProperty(scope, "meta", null);
			ScriptableObject.putProperty(scope, "exports", null);

			cx.evaluateString(scope, script, name, 1, null);

			Scriptable rawMeta = (Scriptable) ScriptableObject.getProperty(scope, "meta");
			ScriptMeta meta = ScriptMeta.loadMeta(name, rawMeta);

			Scriptable exports = (Scriptable) ScriptableObject.getProperty(scope, "exports");
			return new Script(meta, exports);
		} catch (EcmaError ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not enable script '" + name + "' due to error", ex);
			return null;
		}
	}
}
