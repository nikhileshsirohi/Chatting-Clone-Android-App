package com.example.chattingclone.Screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chattingclone.CommonDivider
import com.example.chattingclone.CommonImage
import com.example.chattingclone.CommonProgressBar
import com.example.chattingclone.DestinationScreen
import com.example.chattingclone.LCViewModel
import com.example.chattingclone.NavigateTo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(navController: NavController, vm: LCViewModel) {
    val inProcess = vm.inProgress.value
    if (inProcess) {
        CommonProgressBar()
    } else {
        val userData = vm.userData.value
        var name by rememberSaveable {
            mutableStateOf(userData?.name?:"")
        }
        var phone by rememberSaveable {
         mutableStateOf(userData?.phone?:"")
        }
        Column {
            ProfileContent(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                vm = vm,
                name = name,
                phone = phone,
                onNameChange = {name=it},
                onPhoneChange = {phone=it},
                onSave = {
                         vm.createOrUpdateProfile(name = name, phone = phone)
                },
                onBack = {
                         NavigateTo(navController = navController, DestinationScreen.ChatList.route)
                },
                onLogOut = {
                    vm.logOut()
                    NavigateTo(navController = navController, DestinationScreen.SignUp.route)
                }
            )
            BottomNavigationMenu(
                selectedItem = BottomNavigationMenu.PROFILE,
                navController = navController
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    modifier: Modifier,
    vm: LCViewModel,
    name: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogOut: () -> Unit
) {
    val imageUrl = vm.userData?.value?.imageUrl
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", modifier = Modifier.clickable {
                onBack.invoke()
            })
            Text(text = "Save", modifier = Modifier.clickable {
                onSave.invoke()
            })
        }
        CommonDivider()
            ProfileImage(imageUrl = imageUrl, vm = vm)
            CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name, onValueChange = onNameChange,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Phone", modifier = Modifier.width(100.dp))
            TextField(
                value = phone, onValueChange = onPhoneChange,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent
                )
            )
        }
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        )
        {
            Text(text = "LogOut", modifier = Modifier.clickable { onLogOut.invoke() })
        }
    }
}


@Composable
fun ProfileImage(imageUrl: String?, vm: LCViewModel) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            vm.uploadProfileImage(uri)
        }
    }
    Box(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                CommonImage(data = imageUrl.toString())
            }
            Text(text = "Change Profile Picture")
        }
        if (vm.inProgress.value) {
            CommonProgressBar()
        }
    }
}