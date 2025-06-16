package moon1it.com.confetti

import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.compiler.CompilerManager

@Service(Service.Level.PROJECT)
class CompilationSuccessListener {
    companion object {
        fun getInstance(project: Project): CompilationSuccessListener {
            return project.service<CompilationSuccessListener>()
        }
    }

    // 添加一个标志，用于调试
    private var isListenerAdded = false

    @Suppress("DEPRECATION")
    fun setup(project: Project) {
        if (isListenerAdded) return  // 防止重复添加监听器

        try {
            val compilerManager = CompilerManager.getInstance(project)
            compilerManager.addCompilationStatusListener(object : CompilationStatusListener {
                override fun compilationFinished(aborted: Boolean, errors: Int, warnings: Int, compileContext: CompileContext) {
                    if (!aborted && errors == 0) {
                        // 添加日志输出，以便调试
                        println("编译成功，准备显示彩屑效果")
                        ConfettiEffect.show(project)
                    }
                }
            })
            isListenerAdded = true
            println("成功添加编译监听器")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
