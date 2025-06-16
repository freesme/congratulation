package moon1it.com.confetti

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class ConfettiPlugin : StartupActivity {
    override fun runActivity(project: Project) {
        CompilationSuccessListener.getInstance(project).setup(project)
    }
} 