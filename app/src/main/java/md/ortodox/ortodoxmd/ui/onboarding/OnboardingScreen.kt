package md.ortodox.ortodoxmd.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppPaddings

// ACTUALIZAT: Structura de date acceptă acum orice Composable pentru imagine/iconiță
private data class OnboardingPage(
    val image: @Composable () -> Unit,
    val titleResId: Int,
    val descriptionResId: Int
)

// ACTUALIZAT: Lista folosește noua structură
private val onboardingPages = listOf(
    OnboardingPage(
        image = {
            Image(
                painter = painterResource(id = R.drawable.nav_drawer_banner),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
        },
        titleResId = R.string.onboarding_title_1,
        descriptionResId = R.string.onboarding_desc_1
    ),
    OnboardingPage(
        image = {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        titleResId = R.string.onboarding_title_2,
        descriptionResId = R.string.onboarding_desc_2
    ),
    OnboardingPage(
        image = {
            Icon(
                imageVector = Icons.Default.DownloadForOffline,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        titleResId = R.string.onboarding_title_3,
        descriptionResId = R.string.onboarding_desc_3
    ),
    OnboardingPage(
        image = {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        titleResId = R.string.onboarding_title_4,
        descriptionResId = R.string.onboarding_desc_4
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onOnboardingFinished: () -> Unit) {
    val pagerState = rememberPagerState { onboardingPages.size }
    val scope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = AppPaddings.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = onOnboardingFinished,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(AppPaddings.s)
            ) {
                Text(stringResource(R.string.onboarding_skip))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(page = onboardingPages[pageIndex])
            }

            PagerIndicator(
                pageCount = onboardingPages.size,
                currentPage = pagerState.currentPage
            )

            Spacer(modifier = Modifier.height(AppPaddings.xl))

            AnimatedContent(
                targetState = pagerState.currentPage == onboardingPages.lastIndex,
                label = "button_animation"
            ) { isLastPage ->
                if (isLastPage) {
                    Button(
                        onClick = onOnboardingFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppPaddings.xl)
                    ) {
                        Text(stringResource(R.string.onboarding_finish))
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppPaddings.xl)
                    ) {
                        Text(stringResource(R.string.onboarding_next))
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppPaddings.xl * 2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ACTUALIZAT: Apelăm direct funcția Composable
        page.image()

        Spacer(modifier = Modifier.height(AppPaddings.xl))
        Text(
            text = stringResource(id = page.titleResId),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(AppPaddings.l))
        Text(
            text = stringResource(id = page.descriptionResId),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PagerIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppPaddings.m)
    ) {
        repeat(pageCount) { iteration ->
            val color = if (currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
