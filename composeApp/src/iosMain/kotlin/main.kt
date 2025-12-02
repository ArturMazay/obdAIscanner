import androidx.compose.ui.window.ComposeUIViewController
import com.aistudio.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
