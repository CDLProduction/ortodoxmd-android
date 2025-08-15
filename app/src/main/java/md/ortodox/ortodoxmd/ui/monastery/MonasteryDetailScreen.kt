package md.ortodox.ortodoxmd.ui.monastery

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MonasteryDetailScreen(
    navController: NavController,
    monasteryId: Long,
    viewModel: MonasteryDetailViewModel = hiltViewModel()
) {
    val monastery by viewModel.monastery.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // REFACTORIZAT: Folosim AppScaffold.
    AppScaffold(
        title = stringResource(R.string.monastery_detail_title),
        onBack = { navController.popBackStack() }
    ) { paddingValues ->
        monastery?.let { m ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(AppPaddings.l),
                verticalArrangement = Arrangement.spacedBy(AppPaddings.l)
            ) {
                Text(
                    text = m.nameRo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // REFACTORIZAT: Folosim AppCard pentru descriere.
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = m.formattedDescriptionRo,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(AppPaddings.l),
                        lineHeight = 24.sp
                    )
                }

                // REFACTORIZAT: Folosim AppCard pentru butonul de navigare.
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val encodedName = URLEncoder.encode(m.nameRo, StandardCharsets.UTF_8.toString())
                        val uri = Uri.parse("geo:${m.latitude},${m.longitude}?q=${m.latitude},${m.longitude}($encodedName)")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)

                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            Toast.makeText(context, context.getString(R.string.monastery_no_maps_app_found), Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppPaddings.l),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppPaddings.l)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = stringResource(R.string.monastery_navigate_icon_desc),
                            tint = MaterialTheme.colorScheme.primary // Am ajustat culoarea pentru consistență
                        )
                        Text(
                            text = stringResource(R.string.monastery_navigate_button),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary, // Am ajustat culoarea
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } ?: AppLoading(modifier = Modifier.padding(paddingValues)) // REFACTORIZAT: Folosim AppLoading.
    }
}
