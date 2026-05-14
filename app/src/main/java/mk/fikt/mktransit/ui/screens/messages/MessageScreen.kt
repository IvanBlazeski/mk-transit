package mk.fikt.mktransit.ui.screens.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.fikt.mktransit.R
import mk.fikt.mktransit.domain.model.Conversation
import mk.fikt.mktransit.viewmodel.MessageState
import mk.fikt.mktransit.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    onConversationClick: (String) -> Unit,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.messages)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when (val s = state) {
            is MessageState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MessageState.ConversationsLoaded -> {
                if (s.conversations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Message,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_messages),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.conversations) { conversation ->
                            ConversationCard(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation.conversationId) }
                            )
                        }
                    }
                }
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_messages))
                }
            }
        }
    }
}

@Composable
fun ConversationCard(conversation: Conversation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = conversation.operatorName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    text = "Line: ${conversation.lineName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (conversation.lastMessage.isNotBlank()) {
                    Text(
                        text = conversation.lastMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (conversation.unreadCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = conversation.unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}