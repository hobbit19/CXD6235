#!/bin/bash
# Copyright (C) 2017 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
if [ "$#" -lt 1 ]; then
  echo "Usage profile_pull.sh <packagename>"
  echo "Example: profile_pull.sh com.android.chrome"
  exit 1
fi
package=$1
adb shell kill -s SIGUSR1 $(adb shell pidof $package)
sleep 1 # Leave some time for the profile to save.
#prof_txt_name=$package.prof.txt
prof_name=$package.prof
adb pull data/misc/profiles/cur/0/$package/primary.prof $prof_name
