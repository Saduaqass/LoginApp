package kz.loginapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.*
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.launch
import kz.loginapp.data.*
import java.text.SimpleDateFormat
import java.util.*

private val Navy=Color(0xFF193653); private val Blue=Color(0xFF2563EB)
class AuthModel(app:Application):AndroidViewModel(app){ private val dao=AppDb.get(app).users(); private val prefs=app.getSharedPreferences("auth",0); var user by mutableStateOf<User?>(null); var message by mutableStateOf("")
 init { val id=prefs.getLong("uid",0); if(id>0)viewModelScope.launch{user=dao.byId(id)} }
 fun register(n:String,l:String,p:String,c:String,done:()->Unit){message="";when{listOf(n,l,p,c).any{it.isBlank()}->message="Барлық өрісті толтырыңыз";p.length<8->message="Құпиясөз кемінде 8 таңба болуы керек";p!=c->message="Құпиясөздер сәйкес келмейді";else->viewModelScope.launch{if(dao.byLogin(l)!=null)message="Бұл логин тіркелген" else {val(s,h)=Passwords.make(p);dao.add(User(name=n,login=l,salt=s,hash=h));done()}}} }
 fun login(l:String,p:String,done:()->Unit){message="";if(l.isBlank()||p.isBlank()){message="Барлық өрісті толтырыңыз";return};viewModelScope.launch{val u=dao.byLogin(l);if(u==null||!Passwords.check(p,u))message="Логин немесе құпиясөз қате" else {user=u;prefs.edit().putLong("uid",u.id).apply();message="Сәтті кірдіңіз!";done()}}}
 fun save(n:String){viewModelScope.launch{user?.let{if(n.isNotBlank()){dao.update(it.copy(name=n));user=dao.byId(it.id);message="Сақталды"}}}}
 fun logout(){user=null;prefs.edit().clear().apply()}
}
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(248,250,252);setContent { MaterialTheme(colorScheme=lightColorScheme(primary=Navy,secondary=Blue,background=Color(0xFFF8FAFC))){ val vm:AuthModel= viewModel(); val nav= rememberNavController(); LaunchedEffect(vm.user){if(vm.user!=null&&nav.currentDestination?.route=="home")nav.navigate("dashboard")}; NavHost(nav,"home"){composable("home"){Home(vm,{nav.navigate("login")},{nav.navigate("register")},{nav.navigate("dashboard")})};composable("register"){Register(vm){nav.popBackStack();nav.navigate("login")}};composable("login"){Login(vm){nav.navigate("dashboard"){popUpTo("home"){inclusive=false}}}};composable("dashboard"){if(vm.user==null)LaunchedEffect(Unit){nav.navigate("login"){popUpTo("home")}}else Dashboard(vm,{nav.navigate("profile")},{vm.logout();nav.navigate("login"){popUpTo("home")}})};composable("profile"){if(vm.user==null)LaunchedEffect(Unit){nav.navigate("login")}else Profile(vm){nav.popBackStack()}}} } }}}
@Composable private fun Page(title:String,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.Center,content=content)}
@Composable private fun Brand(){Text("LoginApp",style=MaterialTheme.typography.headlineMedium,color=Navy)}
@Composable private fun Btn(text:String,onClick:()->Unit){Button(onClick,Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(text)}}
@Composable private fun Home(vm:AuthModel,login:()->Unit,register:()->Unit,dash:()->Unit){Page(""){Brand();Spacer(Modifier.height(22.dp));Text("Қош келдіңіз!",style=MaterialTheme.typography.headlineLarge,color=Navy);Text("Тіркеліп, жеке кабинетіңізге кіріңіз.",Modifier.padding(vertical=12.dp));if(vm.user!=null)Btn("Жеке кабинет",dash)else{Btn("Кіру",login);OutlinedButton(register,Modifier.fillMaxWidth()){Text("Тіркелу")}}}}
@Composable private fun Register(vm:AuthModel,done:()->Unit){var n by remember{mutableStateOf("")};var l by remember{mutableStateOf("")};var p by remember{mutableStateOf("")};var c by remember{mutableStateOf("")};Page(""){Brand();Text("Тіркелу",style=MaterialTheme.typography.headlineMedium);Field("Аты-жөні",n){n=it};Field("Логин",l){l=it};Secret("Құпиясөз",p){p=it};Secret("Құпиясөзді растау",c){c=it};Error(vm.message);Btn("Тіркелу"){vm.register(n,l,p,c,done)}}}
@Composable private fun Login(vm:AuthModel,done:()->Unit){var l by remember{mutableStateOf("")};var p by remember{mutableStateOf("")};var show by remember{mutableStateOf(false)};Page(""){Brand();Text("Кіру",style=MaterialTheme.typography.headlineMedium);Field("Логин",l){l=it};OutlinedTextField(p,{p=it},Modifier.fillMaxWidth(),label={Text("Құпиясөз")},visualTransformation=if(show)VisualTransformation.None else PasswordVisualTransformation(),trailingIcon={TextButton({show=!show}){Text(if(show)"Жасыру" else "Көрсету")}});Error(vm.message);Btn("Кіру"){vm.login(l,p,done)}}}
@Composable private fun Field(label:String,value:String,on:(String)->Unit){OutlinedTextField(value,on,Modifier.fillMaxWidth(),label={Text(label)},singleLine=true)}
@Composable private fun Secret(label:String,value:String,on:(String)->Unit){OutlinedTextField(value,on,Modifier.fillMaxWidth(),label={Text(label)},singleLine=true,visualTransformation=PasswordVisualTransformation())}
@Composable private fun Error(s:String){if(s.isNotEmpty())Text(s,modifier=Modifier.padding(vertical=8.dp),color=Color(0xFFB42318))}
@Composable private fun Dashboard(vm:AuthModel,profile:()->Unit,logout:()->Unit){val u=vm.user?:return;Page(""){Brand();Text("Қош келдіңіз, ${u.name}!",style=MaterialTheme.typography.headlineMedium);Text("Аты-жөні: ${u.name}",Modifier.padding(top=18.dp));Text("Логин: ${u.login}");Text("Тіркелген күні: ${date(u.created)}");Text("Авторизациядан өтті",modifier=Modifier.padding(vertical=12.dp),color=Blue);Btn("Профиль",profile);OutlinedButton(logout,Modifier.fillMaxWidth()){Text("Шығу")}}}
@Composable private fun Profile(vm:AuthModel,back:()->Unit){val u=vm.user?:return;var n by remember(u.id){mutableStateOf(u.name)};Page(""){Brand();Text("Профиль",style=MaterialTheme.typography.headlineMedium);Field("Аты-жөні",n){n=it};Text("Логин: ${u.login}",Modifier.padding(top=12.dp));Text("Тіркелген күні: ${date(u.created)}");Error(vm.message);Btn("Сақтау"){vm.save(n)};OutlinedButton(back,Modifier.fillMaxWidth()){Text("Жеке кабинетке қайту")}}}
private fun date(t:Long)=SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(Date(t))
