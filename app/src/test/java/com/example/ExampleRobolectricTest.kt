package com.example

import com.example.localization.StringsLocalization
import com.example.model.AppLanguage
import com.example.model.AudioBitrate
import com.example.model.AudioFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testAppCoreLogic() {
    val title = StringsLocalization.get("app_title", AppLanguage.ENGLISH)
    assertEquals("AudioMorph", title)

    val mp3 = AudioFormat.fromExtension("mp3")
    assertEquals(AudioFormat.MP3, mp3)

    val bitrate = AudioBitrate.fromKbps(192)
    assertEquals(AudioBitrate.KBPS_192, bitrate)
  }
}
