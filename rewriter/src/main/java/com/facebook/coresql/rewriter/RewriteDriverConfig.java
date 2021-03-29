/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.coresql.rewriter;

import com.facebook.airlift.configuration.Config;
import com.google.common.collect.ImmutableSet;

import javax.validation.constraints.NotNull;

import java.util.Set;

import static java.util.Collections.emptySet;

public class RewriteDriverConfig
{
    private Set<Class<? extends Rewriter>> userEnabledRewriters = emptySet();

    @Config("user-enabled-rewriters")
    public RewriteDriverConfig setUserEnabledRewriters(Set<Class<? extends Rewriter>> userEnabledRewriters)
    {
        this.userEnabledRewriters = ImmutableSet.copyOf(userEnabledRewriters);
        return this;
    }

    @NotNull
    public Set<Class<? extends Rewriter>> getUserEnabledRewriters()
    {
        return userEnabledRewriters;
    }
}
