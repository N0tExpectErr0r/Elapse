package com.n0texpecterr0r.elapseasm;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by N0tExpectErr0r at 2019/08/08
 */
public class ElapsePlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        assert appExtension != null;
        appExtension.registerTransform(new ElapseTransform(project));
    }
}
