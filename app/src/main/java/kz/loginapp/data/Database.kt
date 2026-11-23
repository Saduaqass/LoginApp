package kz.loginapp.data
import android.content.Context
import androidx.room.*
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
@Entity(tableName="users", indices=[Index(value=["login"], unique=true)])
data class User(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val login:String,val salt:String,val hash:String,val created:Long=System.currentTimeMillis())
@Dao interface Users { @Query("SELECT * FROM users WHERE login=:login LIMIT 1") suspend fun byLogin(login:String):User?; @Query("SELECT * FROM users WHERE id=:id") suspend fun byId(id:Long):User?; @Insert suspend fun add(user:User):Long; @Update suspend fun update(user:User) }
@Database(entities=[User::class],version=1,exportSchema=false) abstract class AppDb:RoomDatabase(){ abstract fun users():Users; companion object { @Volatile private var instance:AppDb?=null; fun get(c:Context)=instance?: synchronized(this){instance?:Room.databaseBuilder(c,AppDb::class.java,"loginapp.db").build().also{instance=it}} } }
object Passwords { fun make(password:String):Pair<String,String>{val salt=ByteArray(16).also{SecureRandom().nextBytes(it)};return salt.toHex() to derive(password,salt).toHex()}; fun check(p:String,u:User)=derive(p,u.salt.hex()).toHex()==u.hash; private fun derive(p:String,s:ByteArray)=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec(p.toCharArray(),s,120000,256)).encoded; private fun ByteArray.toHex()=joinToString(""){"%02x".format(it)}; private fun String.hex()=chunked(2).map{it.toInt(16).toByte()}.toByteArray() }
