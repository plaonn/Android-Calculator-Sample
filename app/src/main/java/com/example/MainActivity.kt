package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- THEME COLOR SPECIFICATION ---
val CosmicDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9B5DE5), // Neon Purple
    secondary = Color(0xFF00BBF9), // Neon Cyan
    tertiary = Color(0xFFF15BB5), // Neon Pink
    background = Color(0xFF0C0F17), // Deep Space Obsidian
    surface = Color(0xFF161B26), // Space gray
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF232D3F),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

val MintLightColorScheme = lightColorScheme(
    primary = Color(0xFF2A7B4C), // Deep Mint Green
    secondary = Color(0xFF495E51), // Gray Sage
    tertiary = Color(0xFFE67E22), // Soft Orange
    background = Color(0xFFF5F8F6), // Calm off-white
    surface = Color(0xFFEBF2ED), // Soft Mint Surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C2821),
    onSurface = Color(0xFF1C2821),
    surfaceVariant = Color(0xFFDCE6DF),
    onSurfaceVariant = Color(0xFF2C3E35)
)

val NordicDarkColorScheme = darkColorScheme(
    primary = Color(0xFF61AFEF), // Frost Blue
    secondary = Color(0xFFABB2BF), // Ash Gray
    tertiary = Color(0xFF98C379), // Soft Green
    background = Color(0xFF1E222A), // Charcoal Dark
    surface = Color(0xFF282C34), // Subtle lighter charcoal
    onPrimary = Color(0xFF1E222A),
    onSecondary = Color(0xFF1E222A),
    onTertiary = Color(0xFF1E222A),
    onBackground = Color(0xFFABB2BF),
    onSurface = Color(0xFFABB2BF),
    surfaceVariant = Color(0xFF3E4451),
    onSurfaceVariant = Color(0xFFE5C07B)
)

val RetroClassicColorScheme = lightColorScheme(
    primary = Color(0xFF2C3E50), // Vintage slate
    secondary = Color(0xFF7F8C8D), // Concrete gray
    tertiary = Color(0xFFD35400), // Vintage clay red
    background = Color(0xFFDFE4EA), // Classic grey
    surface = Color(0xFFCED6E0), // Solder joint metal grey
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2F3542),
    onSurface = Color(0xFF2F3542),
    surfaceVariant = Color(0xFFA2B4C6),
    onSurfaceVariant = Color(0xFF2F3542)
)

val SleekLightColorScheme = lightColorScheme(
    primary = Color(0xFF381E72), // Elegant Deep Purple
    secondary = Color(0xFFEADDFF), // Soft lavender purple accent
    tertiary = Color(0xFFF2B8B5), // Soft muted peach red
    background = Color(0xFFFDF8FD), // Ultra clean soft lilac tint background
    surface = Color(0xFFFEF7FF), // Distinct rounded-3xl key surfaces
    onPrimary = Color.White,
    onSecondary = Color(0xFF21005D),
    onTertiary = Color(0xFF601410),
    onBackground = Color(0xFF1B1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3EDF7), // Curved bottom layout tint
    onSurfaceVariant = Color(0xFF49454F)
)

enum class CalculatorTheme {
    SYSTEM, COSMIC, MINT, NORDIC, RETRO, SLEEK
}

// --- STATE MANAGER VIEWMODEL ---
class CalculatorViewModel : ViewModel() {
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _realTimeResult = MutableStateFlow("")
    val realTimeResult: StateFlow<String> = _realTimeResult.asStateFlow()

    private val _history = MutableStateFlow<List<Calculation>>(emptyList())
    val history: StateFlow<List<Calculation>> = _history.asStateFlow()

    private val _theme = MutableStateFlow(CalculatorTheme.SLEEK) // Gorgeous design defaults to SLEEK to showcase the Sleek Interface theme
    val theme: StateFlow<CalculatorTheme> = _theme.asStateFlow()

    private val _isScientific = MutableStateFlow(false)
    val isScientific: StateFlow<Boolean> = _isScientific.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    fun appendSymbol(symbol: String) {
        val current = _expression.value
        val operators = listOf("+", "−", "×", "÷", "^")
        
        // Block consecutive redundant operators
        if (symbol in operators) {
            if (current.isEmpty()) {
                if (symbol == "−") {
                    _expression.value = "−"
                }
                return
            }
            val lastChar = current.last().toString()
            if (lastChar in operators || lastChar == "-") {
                _expression.value = current.dropLast(1) + symbol
                updateRealTime()
                return
            }
        }

        // Decimal rule enforcement
        if (symbol == ".") {
            if (current.isEmpty()) {
                _expression.value = "0."
                updateRealTime()
                return
            }
            val segments = current.split(Regex("[+\\-−×÷()^%]|sin|cos|tan|log|ln|√"))
            val lastSegment = segments.lastOrNull() ?: ""
            if (lastSegment.contains(".")) {
                return
            }
        }

        // Functions automatically appending parenthetical starters
        if (symbol in listOf("sin", "cos", "tan", "log", "ln", "√")) {
            _expression.value = current + symbol + "("
            updateRealTime()
            return
        }

        _expression.value = current + symbol
        updateRealTime()
    }

    private fun updateRealTime() {
        val current = _expression.value
        _realTimeResult.value = CalculatorEngine.evaluateRealTime(current)
    }

    fun clearAll() {
        _expression.value = ""
        _realTimeResult.value = ""
    }

    fun backspace() {
        val current = _expression.value
        if (current.isEmpty()) return

        val functions = listOf("sin(", "cos(", "tan(", "log(", "ln(", "√(")
        for (func in functions) {
            if (current.endsWith(func)) {
                _expression.value = current.dropLast(func.length)
                updateRealTime()
                return
            }
        }

        _expression.value = current.dropLast(1)
        updateRealTime()
    }

    fun toggleSign() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "−"
            return
        }

        try {
            if (current.startsWith("−(") && current.endsWith(")")) {
                _expression.value = current.substring(2, current.length - 1)
            } else if (current.startsWith("−") && !current.contains(Regex("[+×÷%]")) && current.count { it == '−' } == 1) {
                _expression.value = current.substring(1)
            } else if (!current.contains(Regex("[+−×÷()^%]|sin|cos|tan|log|ln|√"))) {
                _expression.value = "−$current"
            } else {
                _expression.value = "−($current)"
            }
            updateRealTime()
        } catch (e: Exception) {
            _expression.value = "−($current)"
            updateRealTime()
        }
    }

    fun appendParenthesis() {
        val current = _expression.value
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }
        // Evaluate contextual bracket appending
        if (openCount > closeCount && current.isNotEmpty() && current.last() != '(' && current.last().toString() !in listOf("+", "−", "×", "÷", "^")) {
            appendSymbol(")")
        } else {
            appendSymbol("(")
        }
    }

    fun onEqualsPressed() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        try {
            var sanitized = expr
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")

            val openCount = sanitized.count { it == '(' }
            val closeCount = sanitized.count { it == ')' }
            if (openCount > closeCount) {
                sanitized += ")".repeat(openCount - closeCount)
            }

            val finalExpr = CalculatorEngine.preprocessExpression(sanitized)
            val resultVal = CalculatorEngine.evaluate(finalExpr)

            if (resultVal.isNaN() || resultVal.isInfinite()) {
                _realTimeResult.value = "Error"
                return
            }

            val formattedResult = CalculatorEngine.formatResult(resultVal)

            val newCalc = Calculation(
                formula = expr + if (openCount > closeCount) ")".repeat(openCount - closeCount) else "",
                result = formattedResult
            )
            _history.value = listOf(newCalc) + _history.value

            _expression.value = formattedResult
            _realTimeResult.value = ""
        } catch (e: Exception) {
            _realTimeResult.value = "Error"
        }
    }

    fun selectHistoryItem(item: Calculation) {
        _expression.value = item.result
        _realTimeResult.value = ""
        _showHistory.value = false
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    fun toggleScientific() {
        _isScientific.value = !_isScientific.value
    }

    fun toggleHistory() {
        _showHistory.value = !_showHistory.value
    }

    fun selectTheme(newTheme: CalculatorTheme) {
        _theme.value = newTheme
    }
}

// --- MAIN ENTRANCE ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CalculatorViewModel = viewModel()
            CalculatorApp(viewModel = viewModel)
        }
    }
}

@Composable
fun CalculatorApp(viewModel: CalculatorViewModel) {
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val realTimeResult by viewModel.realTimeResult.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val activeTheme by viewModel.theme.collectAsStateWithLifecycle()
    val isScientific by viewModel.isScientific.collectAsStateWithLifecycle()
    val showHistory by viewModel.showHistory.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }

    val colorScheme = when (activeTheme) {
        CalculatorTheme.SYSTEM -> if (isSystemInDarkTheme()) CosmicDarkColorScheme else SleekLightColorScheme
        CalculatorTheme.COSMIC -> CosmicDarkColorScheme
        CalculatorTheme.MINT -> MintLightColorScheme
        CalculatorTheme.NORDIC -> NordicDarkColorScheme
        CalculatorTheme.RETRO -> RetroClassicColorScheme
        CalculatorTheme.SLEEK -> SleekLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isLandscape = maxWidth > maxHeight || maxWidth > 600.dp
                
                if (isLandscape) {
                    LandscapeLayout(
                        expression = expression,
                        realTimeResult = realTimeResult,
                        history = history,
                        isScientific = isScientific,
                        showHistory = showHistory,
                        colorScheme = colorScheme,
                        activeTheme = activeTheme,
                        viewModel = viewModel,
                        onThemeDialogTrigger = { showThemeDialog = true }
                    )
                } else {
                    PortraitLayout(
                        expression = expression,
                        realTimeResult = realTimeResult,
                        history = history,
                        isScientific = isScientific,
                        showHistory = showHistory,
                        colorScheme = colorScheme,
                        activeTheme = activeTheme,
                        viewModel = viewModel,
                        onThemeDialogTrigger = { showThemeDialog = true }
                    )
                }
            }

            if (showThemeDialog) {
                ThemeSelectionDialog(
                    currentTheme = activeTheme,
                    onThemeSelect = { viewModel.selectTheme(it) },
                    onDismiss = { showThemeDialog = false },
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
fun PortraitLayout(
    expression: String,
    realTimeResult: String,
    history: List<Calculation>,
    isScientific: Boolean,
    showHistory: Boolean,
    colorScheme: ColorScheme,
    activeTheme: CalculatorTheme,
    viewModel: CalculatorViewModel,
    onThemeDialogTrigger: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section (Header + Display) with dedicated 16.dp screen padding
            Column(
                modifier = Modifier
                    .weight(if (isScientific) 1.2f else 1.5f)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
            ) {
                HeaderRow(
                    activeTheme = activeTheme,
                    isScientific = isScientific,
                    onThemeSelect = { viewModel.selectTheme(it) },
                    onScientificToggle = { viewModel.toggleScientific() },
                    onHistoryToggle = { viewModel.toggleHistory() },
                    onThemeDialogTrigger = onThemeDialogTrigger,
                    colorScheme = colorScheme
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DisplayScreen(
                    expression = expression,
                    realTimeResult = realTimeResult,
                    colorScheme = colorScheme,
                    activeTheme = activeTheme,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Bottom Cabinet Section (Keypad Area)
            Surface(
                modifier = Modifier
                    .weight(if (isScientific) 3.5f else 3.2f)
                    .fillMaxWidth(),
                shape = if (activeTheme == CalculatorTheme.SLEEK) {
                    RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                } else {
                    RoundedCornerShape(0.dp)
                },
                color = if (activeTheme == CalculatorTheme.SLEEK) {
                    colorScheme.surfaceVariant
                } else {
                    Color.Transparent
                },
                shadowElevation = if (activeTheme == CalculatorTheme.SLEEK) 8.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    val sciWeight by animateFloatAsState(
                        targetValue = if (isScientific) 3f else 0f,
                        label = "sciWeight"
                    )

                    if (sciWeight > 0f) {
                        PortraitScientificKeypad(
                            viewModel = viewModel, 
                            colorScheme = colorScheme,
                            keepSquare = false,
                            modifier = Modifier
                                .weight(sciWeight)
                                .padding(bottom = 8.dp)
                                .graphicsLayer { alpha = (sciWeight / 3f).coerceIn(0f, 1f) }
                        )
                    }
                    
                    StandardKeypad(
                        viewModel = viewModel,
                        colorScheme = colorScheme,
                        keepSquare = !isScientific, 
                        modifier = Modifier.weight(5f)
                    )
                }
            }
        }
        
        AnimatedVisibility(
            visible = showHistory,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            HistoryPanel(
                history = history,
                onSelect = { viewModel.selectHistoryItem(it) },
                onClear = { viewModel.clearHistory() },
                onClose = { viewModel.toggleHistory() },
                colorScheme = colorScheme
            )
        }
    }
}

@Composable
fun LandscapeScientificKeypad(
    viewModel: CalculatorViewModel,
    colorScheme: ColorScheme,
    keepSquare: Boolean = true,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.theme.collectAsStateWithLifecycle()
    val sciKeys = listOf(
        listOf("sin", "cos", "tan"),
        listOf("log", "ln", "e"),
        listOf("√", "π", "^"),
        listOf("(", ")", ""),
        listOf("", "", "")
    )
    
    val buttonShape = if (activeTheme == CalculatorTheme.SLEEK) {
        RoundedCornerShape(24.dp)
    } else {
        CircleShape
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        for (row in sciKeys) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (text in row) {
                    if (text.isEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        CalculatorButton(
                            text = text,
                            onClick = { viewModel.appendSymbol(text) },
                            containerColor = colorScheme.secondaryContainer,
                            contentColor = colorScheme.onSecondaryContainer,
                            isActionButton = false,
                            fontSize = 14.sp,
                            shape = buttonShape,
                            keepSquare = keepSquare,
                            modifier = Modifier
                                .weight(1f)
                                .then(if (!keepSquare) Modifier.fillMaxHeight() else Modifier)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LandscapeLayout(
    expression: String,
    realTimeResult: String,
    history: List<Calculation>,
    isScientific: Boolean,
    showHistory: Boolean,
    colorScheme: ColorScheme,
    activeTheme: CalculatorTheme,
    viewModel: CalculatorViewModel,
    onThemeDialogTrigger: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // 헤더 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("=", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text(
                        text = "계산기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 테마 모달을 띄우는 크고 누르기 편한 세련된 전용 버튼
                    IconButton(
                        onClick = onThemeDialogTrigger,
                        modifier = Modifier
                            .size(40.dp)
                            .background(colorScheme.surfaceVariant, CircleShape)
                            .testTag("btn_theme_dialog_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "테마 선택",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleScientific() },
                        modifier = Modifier
                            .testTag("btn_sci_toggle")
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScientific) colorScheme.primary else colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "공학함수",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isScientific) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleHistory() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .testTag("btn_history_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "기록 열기",
                            tint = colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 디스플레이 영역 (비율에 맞춰 깔끔하게 가득 차도록 설계)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DisplayScreen(
                    expression = expression,
                    realTimeResult = realTimeResult,
                    colorScheme = colorScheme,
                    activeTheme = activeTheme,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 키패드 보드 하단 섹션
            Box(
                modifier = Modifier
                    .weight(3.0f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    shape = if (activeTheme == CalculatorTheme.SLEEK) {
                        RoundedCornerShape(24.dp)
                    } else {
                        RoundedCornerShape(0.dp)
                    },
                    color = if (activeTheme == CalculatorTheme.SLEEK) {
                        colorScheme.surfaceVariant
                    } else {
                        Color.Transparent
                    },
                    shadowElevation = if (activeTheme == CalculatorTheme.SLEEK) 4.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isScientific) {
                            LandscapeScientificKeypad(
                                viewModel = viewModel,
                                colorScheme = colorScheme,
                                keepSquare = false, 
                                modifier = Modifier
                                    .weight(1.3f)
                                    .fillMaxHeight()
                            )
                        }
                        
                        StandardKeypad(
                            viewModel = viewModel,
                            colorScheme = colorScheme,
                            keepSquare = false, 
                            modifier = Modifier
                                .weight(1.7f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }

        // 세로 및 가로에 일괄 적용되는 계산 역사 모달
        AnimatedVisibility(
            visible = showHistory,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            HistoryPanel(
                history = history,
                onSelect = { viewModel.selectHistoryItem(it) },
                onClear = { viewModel.clearHistory() },
                onClose = { viewModel.toggleHistory() },
                colorScheme = colorScheme
            )
        }
    }
}

@Composable
fun HeaderRow(
    activeTheme: CalculatorTheme,
    isScientific: Boolean,
    onThemeSelect: (CalculatorTheme) -> Unit,
    onScientificToggle: () -> Unit,
    onHistoryToggle: () -> Unit,
    onThemeDialogTrigger: () -> Unit,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("=", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text(
                text = "계산기",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 크고 시원하게 테마 팝업을 열 수 있는 팔레트 버튼
            IconButton(
                onClick = onThemeDialogTrigger,
                modifier = Modifier
                    .size(40.dp)
                    .background(colorScheme.surfaceVariant, CircleShape)
                    .testTag("btn_theme_dialog_trigger")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "테마 선택",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Button(
                onClick = onScientificToggle,
                modifier = Modifier
                    .testTag("btn_sci_toggle")
                    .height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScientific) colorScheme.primary else colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "공학",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isScientific) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onHistoryToggle,
                modifier = Modifier
                    .size(36.dp)
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    .testTag("btn_history_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "기록 열기",
                    tint = colorScheme.onBackground.copy(alpha = 0.61f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DisplayScreen(
    expression: String,
    realTimeResult: String,
    colorScheme: ColorScheme,
    activeTheme: CalculatorTheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (activeTheme == CalculatorTheme.SLEEK) Color.Transparent else colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            val scrollState = rememberScrollState()

            Text(
                text = if (expression.isEmpty()) "0" else expression,
                fontSize = if (expression.length > 12) 30.sp else 40.sp,
                fontWeight = if (activeTheme == CalculatorTheme.SLEEK) FontWeight.Light else FontWeight.Medium,
                color = if (activeTheme == CalculatorTheme.SLEEK) colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else colorScheme.onBackground,
                fontFamily = if (activeTheme == CalculatorTheme.SLEEK) FontFamily.SansSerif else FontFamily.Monospace,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                lineHeight = 48.sp
            )

            LaunchedEffect(expression) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            if (realTimeResult.isNotEmpty()) {
                Text(
                    text = realTimeResult,
                    fontSize = if (activeTheme == CalculatorTheme.SLEEK) 54.sp else 24.sp,
                    fontWeight = if (activeTheme == CalculatorTheme.SLEEK) FontWeight.Light else FontWeight.Bold,
                    color = if (activeTheme == CalculatorTheme.SLEEK) colorScheme.onBackground else colorScheme.primary,
                    fontFamily = if (activeTheme == CalculatorTheme.SLEEK) FontFamily.SansSerif else FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_result")
                )
            } else {
                Spacer(modifier = Modifier.height(28.dp))
            }

            if (activeTheme == CalculatorTheme.SLEEK) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 12.dp)
                        .width(48.dp)
                        .height(4.dp)
                        .background(colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                )
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isActionButton: Boolean = false,
    fontSize: TextUnit = 22.sp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    keepSquare: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "BtnScale"
    )
    
    val haptic = LocalHapticFeedback.current
    val tag = when (text) {
        "+" -> "plus"
        "−" -> "minus"
        "×" -> "multiply"
        "÷" -> "divide"
        "=" -> "equals"
        "AC" -> "ac"
        "⌫" -> "backspace"
        "." -> "decimal"
        else -> text.lowercase()
    }

    val buttonModifier = modifier
        .padding(4.dp)
        .let { if (keepSquare) it.aspectRatio(1f) else it }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = if (isPressed) 1.dp else 2.dp,
            shape = shape,
            clip = false
        )
        .background(
            color = if (isPressed) containerColor.copy(alpha = 0.85f) else containerColor,
            shape = shape
        )
        .clip(shape)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        )
        .testTag("btn_$tag")

    Box(
        contentAlignment = Alignment.Center,
        modifier = buttonModifier
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = fontSize,
            fontWeight = if (isActionButton) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
fun StandardKeypad(
    viewModel: CalculatorViewModel,
    colorScheme: ColorScheme,
    keepSquare: Boolean = true,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.theme.collectAsStateWithLifecycle()
    val keys = listOf(
        listOf("AC" to true, "⌫" to false, "%" to false, "÷" to true),
        listOf("7" to false, "8" to false, "9" to false, "×" to true),
        listOf("4" to false, "5" to false, "6" to false, "−" to true),
        listOf("1" to false, "2" to false, "3" to false, "+" to true),
        listOf("+/-" to false, "0" to false, "." to false, "=" to true)
    )

    Column(modifier = modifier.fillMaxSize()) {
        for (row in keys) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for ((text, isAction) in row) {
                    val containerColor = when {
                        text == "=" -> colorScheme.primary
                        text == "AC" -> colorScheme.tertiary
                        text == "⌫" || text == "+/-" || text == "%" -> colorScheme.surfaceVariant
                        text in listOf("÷", "×", "−", "+") -> colorScheme.secondary
                        else -> colorScheme.surface
                    }
                    val contentColor = when {
                        text == "=" -> colorScheme.onPrimary
                        text == "AC" -> colorScheme.onTertiary
                        text == "⌫" || text == "+/-" || text == "%" -> colorScheme.onSurfaceVariant
                        text in listOf("÷", "×", "−", "+") -> colorScheme.onSecondary
                        else -> colorScheme.onSurface
                    }

                    val buttonShape = if (activeTheme == CalculatorTheme.SLEEK) {
                        if (text in listOf("7", "8", "9", "4", "5", "6", "1", "2", "3", "0", ".", "+/-")) {
                            RoundedCornerShape(24.dp)
                        } else {
                            CircleShape
                        }
                    } else {
                        CircleShape
                    }

                    CalculatorButton(
                        text = text,
                        onClick = {
                            when (text) {
                                "AC" -> viewModel.clearAll()
                                "⌫" -> viewModel.backspace()
                                "+/-" -> viewModel.toggleSign()
                                "=" -> viewModel.onEqualsPressed()
                                else -> viewModel.appendSymbol(text)
                            }
                        },
                        containerColor = containerColor,
                        contentColor = contentColor,
                        isActionButton = isAction,
                        shape = buttonShape,
                        keepSquare = keepSquare,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (!keepSquare) Modifier.fillMaxHeight() else Modifier)
                    )
                }
            }
        }
    }
}

@Composable
fun PortraitScientificKeypad(
    viewModel: CalculatorViewModel,
    colorScheme: ColorScheme,
    keepSquare: Boolean = true,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.theme.collectAsStateWithLifecycle()
    val sciKeys = listOf(
        listOf("sin", "cos", "tan", "log"),
        listOf("ln", "e", "√", "π"),
        listOf("^", "(", ")", "")
    )
    
    val buttonShape = if (activeTheme == CalculatorTheme.SLEEK) {
        RoundedCornerShape(24.dp)
    } else {
        CircleShape
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        for (row in sciKeys) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (text in row) {
                    if (text.isEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        CalculatorButton(
                            text = text,
                            onClick = { viewModel.appendSymbol(text) },
                            containerColor = colorScheme.secondaryContainer,
                            contentColor = colorScheme.onSecondaryContainer,
                            isActionButton = false,
                            fontSize = 14.sp,
                            shape = buttonShape,
                            keepSquare = keepSquare,
                            modifier = Modifier
                                .weight(1f)
                                .then(if (!keepSquare) Modifier.fillMaxHeight() else Modifier)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryPanel(
    history: List<Calculation>,
    onSelect: (Calculation) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    colorScheme: ColorScheme
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                onClick = onClose,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "계산 기록",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (history.isNotEmpty()) {
                            Text(
                                text = "전체 삭제",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.error,
                                modifier = Modifier
                                    .clickable { onClear() }
                                    .padding(8.dp)
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "기록 닫기",
                                tint = colorScheme.onSurface
                            )
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "기록 비어있음",
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "계산 기록이 비어있습니다.",
                                fontSize = 15.sp,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(history) { item ->
                            HistoryItem(
                                calculation = item,
                                onClick = { onSelect(item) },
                                colorScheme = colorScheme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    calculation: Calculation,
    onClick: () -> Unit,
    colorScheme: ColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = calculation.formula,
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "= ${calculation.result}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: CalculatorTheme,
    onThemeSelect: (CalculatorTheme) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "테마 선택",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val themes = listOf(
                        Triple(CalculatorTheme.SLEEK, "Sleek 퍼플", Color(0xFF381E72)),
                        Triple(CalculatorTheme.COSMIC, "Cosmic 바이올렛", Color(0xFF9B5DE5)),
                        Triple(CalculatorTheme.MINT, "Green 민트", Color(0xFF2A7B4C)),
                        Triple(CalculatorTheme.NORDIC, "Nordic 스카이", Color(0xFF61AFEF)),
                        Triple(CalculatorTheme.RETRO, "Classic 그레이", Color(0xFF7F8C8D)),
                        Triple(CalculatorTheme.SYSTEM, "시스템 기본", Color.Gray)
                    )
                    
                    themes.forEach { (theme, name, color) ->
                        val isSelected = currentTheme == theme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    onThemeSelect(theme)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(color, CircleShape)
                                )
                                Text(
                                    text = name,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "선택됨",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("닫기", color = colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

