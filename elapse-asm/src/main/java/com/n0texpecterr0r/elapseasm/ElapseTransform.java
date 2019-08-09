package com.n0texpecterr0r.elapseasm;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Created by N0tExpectErr0r at 2019/08/08
 */
public class ElapseTransform extends Transform {
    private Map<String, File> modifyMap = new HashMap<>();
    private Project project;

    public ElapseTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return ElapseTransform.class.getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        if (!isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }
        // 获取输入（消费型输入，需要传递给下一个Transform）
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        for (TransformInput input : inputs) {
            // 遍历输入，分别遍历其中的jar以及directory
            for (JarInput jarInput : input.getJarInputs()) {
                // 对jar文件进行处理
                transformJar(transformInvocation, jarInput);
            }
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                // 对directory进行处理
                transformDirectory(transformInvocation, directoryInput);
            }
        }
    }

    private void transformJar(TransformInvocation invocation, JarInput input) throws IOException {
        File tempDir = invocation.getContext().getTemporaryDir();
        String destName = input.getFile().getName();
        String hexName = DigestUtils.md5Hex(input.getFile().getAbsolutePath()).substring(0, 8);
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4);
        }
        // 获取输出路径
        File dest = invocation.getOutputProvider()
                .getContentLocation(destName + "_" + hexName, input.getContentTypes(), input.getScopes(), Format.JAR);

        JarFile originJar = new JarFile(input.getFile());
        File outputJar = new File(tempDir, "temp_"+input.getFile().getName());
        JarOutputStream output = new JarOutputStream(new FileOutputStream(outputJar));

        // 遍历原jar文件寻找class文件
        Enumeration<JarEntry> enumeration = originJar.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry originEntry = enumeration.nextElement();
            InputStream inputStream = originJar.getInputStream(originEntry);

            String entryName = originEntry.getName();
            if (entryName.endsWith(".class")) {
                JarEntry destEntry = new JarEntry(entryName);
                output.putNextEntry(destEntry);

                byte[] sourceBytes = IOUtils.toByteArray(inputStream);
                // 修改class文件内容
                byte[] modifiedBytes = modifyClass(sourceBytes);
                if (modifiedBytes == null) {
                    modifiedBytes = sourceBytes;
                }
                output.write(modifiedBytes);
                output.closeEntry();
            }
        }
        output.close();
        originJar.close();

        // 复制修改后jar到输出路径
        FileUtils.copyFile(outputJar, dest);
    }

    private void transformDirectory(TransformInvocation invocation, DirectoryInput input) throws IOException {
        File tempDir = invocation.getContext().getTemporaryDir();
        // 获取输出路径
        File dest = invocation.getOutputProvider()
                .getContentLocation(input.getName(), input.getContentTypes(), input.getScopes(), Format.DIRECTORY);
        File dir = input.getFile();
        if (dir != null && dir.exists()) {
            traverseDirectory(tempDir, dir);
            FileUtils.copyDirectory(input.getFile(), dest);
            for (Map.Entry<String, File> entry : modifyMap.entrySet()) {
                File target = new File(dest.getAbsolutePath() + entry.getKey());
                if (target.exists()) {
                    target.delete();
                }
                FileUtils.copyFile(entry.getValue(), target);
                entry.getValue().delete();
            }
        }
    }

    private void traverseDirectory(File tempDir, File dir) throws IOException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                traverseDirectory(tempDir, file);
            } else if (file.getAbsolutePath().endsWith(".class")) {
                String className = path2ClassName(file.getAbsolutePath()
                        .replace(dir.getAbsolutePath() + File.separator, ""));
                byte[] sourceBytes = IOUtils.toByteArray(new FileInputStream(file));
                byte[] modifiedBytes = modifyClass(sourceBytes);
                File modified = new File(tempDir, className.replace(".", "") + ".class");
                if (modified.exists()) {
                    modified.delete();
                }
                modified.createNewFile();
                new FileOutputStream(modified).write(modifiedBytes);
                String key = file.getAbsolutePath().replace(dir.getAbsolutePath(), "");
                modifyMap.put(key, modified);
            }
        }
    }

    private byte[] modifyClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ElapseClassVisitor(classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    static String path2ClassName(String pathName) {
        return pathName.replace(File.separator, ".").replace(".class", "");
    }
}
