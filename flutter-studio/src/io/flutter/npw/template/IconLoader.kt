package io.flutter.npw.template

import com.android.tools.idea.npw.ui.TemplateIcon
import com.google.common.cache.CacheLoader
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.IconLoader.findIcon
import java.net.URL
import javax.swing.Icon
import java.util.Optional

private val log get() = logger<IconLoader>()

/**
 * Guava [CacheLoader] which can convert a file path to an icon. This is used to help us load standard 256x256 icons out of template files.
 *
 * Note: optional [Icon] is used instead of nullable [Icon] because null is a special value in cacheLoader and should not be used.
 */
internal class IconLoader : CacheLoader<URL, Optional<Icon>>() {
  override fun load(iconPath: URL): Optional<Icon> {
    val icon = findIcon(iconPath) ?: run {
      log.warn("${iconPath} could not be found or is not a valid image")
      return Optional.empty()
    }
    return Optional.of(
      TemplateIcon(icon).apply {
        cropBlankWidth()
        setHeight(256)
      })
  }
}
