/*
 * Copyright (C) 2022 KanYun
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

package com.kanyun.kace

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

sealed interface AndroidExtensionsComponent {
    fun <V : View?> findViewById(id: Int): V?
}

fun AndroidExtensionsComponent(
    owner: AndroidExtensionsBase,
    onDestroy: () -> Unit
): AndroidExtensionsComponent {
    return when (owner) {
        is Activity -> AndroidExtensionsActivity(owner, onDestroy)
        is Fragment -> AndroidExtensionsFragment(owner, onDestroy)
        else -> throw UnsupportedOperationException()
    }
}

class AndroidExtensionsActivity(
    private val activity: Activity,
    onDestroy: () -> Unit
) : AndroidExtensionsComponent {

    init {
        if (activity is LifecycleOwner) {
            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    onDestroy()
                }
            })
        }
    }

    override fun <V : View?> findViewById(id: Int): V? {
        return activity.findViewById(id)
    }
}

class AndroidExtensionsFragment(
    private val fragment: Fragment,
    onDestroy: () -> Unit
) : AndroidExtensionsComponent {

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) {
            it?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    onDestroy()
                }
            })
        }
    }

    override fun <V : View?> findViewById(id: Int): V? {
        return fragment.view?.findViewById(id)
    }
}