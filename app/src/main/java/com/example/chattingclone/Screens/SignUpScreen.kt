package com.example.chattingclone.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chattingclone.CommonProgressBar
import com.example.chattingclone.DestinationScreen
import com.example.chattingclone.LCViewModel
import com.example.chattingclone.R
import com.example.chattingclone.CheckSignedIn
import com.example.chattingclone.NavigateTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, vm: LCViewModel) {
    CheckSignedIn(vm = vm, navController = navController)
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nameState = remember {
                mutableStateOf(TextFieldValue())
            }
            val numberState = remember {
                mutableStateOf(TextFieldValue())
            }
            val emailState = remember {
                mutableStateOf(TextFieldValue())
            }
            val passwordState = remember {
                mutableStateOf(TextFieldValue())
            }
            val focus = LocalFocusManager.current
            Image(
                painter =
                painterResource(id = R.drawable.meetme),
                contentDescription = null,
                modifier = Modifier
                    .width(200.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )
            Text(
                text = "SignUp",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(
                value = nameState.value,
                onValueChange = {
                    nameState.value = it
                },
                label = {Text(text = "Name")},
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(
                value = numberState.value,
                onValueChange = {
                    numberState.value = it
                },
                label = {Text(text = "Phone")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(
                value = emailState.value,
                onValueChange = {
                    emailState.value = it
                },
                label = {Text(text = "Email")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = {
                    passwordState.value = it
                },
                label = {Text(text = "Password")},
                modifier = Modifier.padding(8.dp),
                visualTransformation = PasswordVisualTransformation()
            )
            Button(onClick = { vm.signUp(context,
                nameState.value.text,
                emailState.value.text,
                numberState.value.text,
                passwordState.value.text
            ) },
                modifier = Modifier.padding(8.dp)) {
                Text(text = "SignUp")
            }

            Text(text = "Already a user ? Login - >",
                color = Color.Red,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        NavigateTo(navController, DestinationScreen.Login.route)
                    }
            )
        }
    }
    if(vm.inProgress.value){
        CommonProgressBar()
    }
}