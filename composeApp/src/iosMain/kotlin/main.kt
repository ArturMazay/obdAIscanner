import androidx.compose.ui.window.ComposeUIViewController
import com.aistudio.ui.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
