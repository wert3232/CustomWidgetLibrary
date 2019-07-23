package cn.qqtheme.framework.picker

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.appcompat.content.res.AppCompatResources
import android.view.Gravity
import android.view.View
import android.widget.*

import com.common.*
import com.library.R

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import cn.qqtheme.framework.adapter.FileAdapter
import cn.qqtheme.framework.adapter.PathAdapter
import cn.qqtheme.framework.popup.BasicPopup
import cn.qqtheme.framework.popup.ConfirmPopup
import cn.qqtheme.framework.util.ConvertUtils
import cn.qqtheme.framework.util.LogUtils
import cn.qqtheme.framework.util.StorageUtils
import cn.qqtheme.framework.widget.HorizontalListView

/**
 * 文件目录选择器
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/9/29, 2017/01/01, 2017/01/08
 */
class FilePicker(activity: Activity, @param:Mode private val mode: Int) : ConfirmPopup<LinearLayout>(activity), AdapterView.OnItemClickListener {

    private var initPath: String? = null
    val adapter = FileAdapter()
    val pathAdapter = PathAdapter()
    private var emptyView: View? = null
    private var emptyText: TextView? = null
    private var onFilePickListener: OnFilePickListener? = null
    private var emptyHint: CharSequence = if (java.util.Locale.getDefault().displayLanguage.contains("中文")) "<空>" else "<Empty>"

    val currentPath: String
        get() = adapter.currentPath

    @IntDef(value = intArrayOf(DIRECTORY, FILE))
    @Retention(RetentionPolicy.SOURCE)
    annotation class Mode

    init {
        setHalfScreen(true)
        try {
            this.initPath = StorageUtils.getDownloadPath()
        } catch (e: RuntimeException) {
            this.initPath = StorageUtils.getInternalRootPath(activity)
        }

        adapter.isOnlyListDir = mode == DIRECTORY
        adapter.isShowHideDir = false
        adapter.isShowHomeDir = false
        adapter.isShowUpDir = false
    }

    fun setEmptyView(emptyView: View): FilePicker {
        this.emptyView = emptyView
        return this
    }

    override fun makeCenterView(): View {
        val rootLayout = ConstraintLayout(activity)
        rootLayout.id = View.generateViewId()
        rootLayout.layoutParams = ConstraintLayout.LayoutParams(BasicPopup.MATCH_PARENT, BasicPopup.MATCH_PARENT)
        rootLayout.setBackgroundColor(0x11000000)

        val side = ScrollView(activity).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(240, BasicPopup.MATCH_PARENT).apply {
                setMargins(16, 16, 16, 16)
                topToTop = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }
        }
        val storageParent = LinearLayout(activity).apply {
            id = View.generateViewId()
            gravity = Gravity.CENTER_HORIZONTAL
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, BasicPopup.WRAP_CONTENT)
        }
        side.addView(storageParent)

        activity.getStorageList()?.also { list ->
            for (i in list.reversed().indices) {
                val path = list[i].path
                val tx = TextView(activity).apply {
                    setTextColor(AppCompatResources.getColorStateList(activity, R.color.storage_text_color))
                    setBackgroundColor(Color.WHITE)
                    val name = if (!list[i].isRemoveAble) {
                        val sd = Environment.getExternalStorageDirectory().absolutePath
                        if (sd.startsWith(list[i].path) || list[i].path.startsWith(sd)) {
                            context.getString(R.string.internal_sd)
                        } else {
                            context.getString(R.string.internal_storage)
                        }
                    } else {
                        context.getString(R.string.sd_or_u)
                    }
                    text = "$name"
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(BasicPopup.MATCH_PARENT, BasicPopup.WRAP_CONTENT).apply {
                        topMargin = if (i == 0) 0 else 10
                        bottomMargin = 10
                    }
                    setCompatElevation(8f)
                }
                AppCompatResources.getDrawable(activity, R.drawable.storage)?.also { img ->
                    img.setBounds(0, 0, 150, 150)
                    tx.setCompoundDrawables(null, img, null, null)
                }

                tx.setOnClickListener { setDirectoryPath(path) }
                storageParent.addView(tx)
            }
        }

        rootLayout.addView(side)

        val listView = ListView(activity).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            divider = ColorDrawable(-0x222223)
            dividerHeight = 1
            cacheColorHint = Color.TRANSPARENT
            setCompatElevation(8f)
            layoutParams = ConstraintLayout.LayoutParams(0, BasicPopup.MATCH_PARENT).apply {
                setMargins(16, 16, 16, 16)
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                startToEnd = side.id
                endToEnd = ConstraintSet.PARENT_ID
            }
        }
        listView.adapter = adapter
        listView.onItemClickListener = this
        rootLayout.addView(listView)

        emptyText = TextView(activity).apply {
            gravity = Gravity.CENTER
            visibility = View.GONE
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
        }
        if (emptyView == null) {
            emptyView = emptyText
        }
        emptyView?.apply {
            id = View.generateViewId()
            setCompatElevation(8f)
            layoutParams = ConstraintLayout.LayoutParams(0, BasicPopup.MATCH_PARENT).apply {
                setMargins(16, 16, 16, 16)
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                startToEnd = side.id
                endToEnd = ConstraintSet.PARENT_ID
            }
        }

        rootLayout.addView(emptyView)

        return rootLayout
    }

    override fun makeFooterView(): View? {
        val rootLayout = LinearLayout(activity)
        rootLayout.layoutParams = LinearLayout.LayoutParams(BasicPopup.MATCH_PARENT, BasicPopup.WRAP_CONTENT)
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.setBackgroundColor(Color.WHITE)

        val lineView = View(activity)
        lineView.layoutParams = LinearLayout.LayoutParams(BasicPopup.MATCH_PARENT, 1)
        lineView.setBackgroundColor(-0x222223)
        rootLayout.addView(lineView)

        val pathView = HorizontalListView(activity)
        val height = ConvertUtils.toPx(activity, 30f)
        pathView.layoutParams = LinearLayout.LayoutParams(BasicPopup.MATCH_PARENT, height)
        pathView.adapter = pathAdapter
        pathView.setBackgroundColor(Color.WHITE)
        pathView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> refreshCurrentDirPath(pathAdapter.getItem(position)) }
        rootLayout.addView(pathView)

        return rootLayout
    }

    fun setRootPath(initPath: String) {
        this.initPath = initPath
    }

    fun setAllowExtensions(allowExtensions: Array<String>) {
        adapter.setAllowExtensions(allowExtensions)
    }

    fun setShowUpDir(showUpDir: Boolean) {
        adapter.isShowUpDir = showUpDir
    }

    fun setShowHomeDir(showHomeDir: Boolean) {
        adapter.isShowHomeDir = showHomeDir
    }

    fun setShowHideDir(showHideDir: Boolean) {
        adapter.isShowHideDir = showHideDir
    }

    fun setFileIcon(fileIcon: Drawable) {
        adapter.setFileIcon(fileIcon)
    }

    fun setFolderIcon(folderIcon: Drawable) {
        adapter.setFolderIcon(folderIcon)
    }

    fun setHomeIcon(homeIcon: Drawable) {
        adapter.setHomeIcon(homeIcon)
    }

    fun setUpIcon(upIcon: Drawable) {
        adapter.setUpIcon(upIcon)
    }

    fun setArrowIcon(arrowIcon: Drawable) {
        pathAdapter.setArrowIcon(arrowIcon)
    }

    fun setItemHeight(itemHeight: Int) {
        adapter.setItemHeight(itemHeight)
    }

    fun setEmptyHint(emptyHint: CharSequence) {
        this.emptyHint = emptyHint
    }

    override fun setContentViewBefore() {
        val isPickFile = mode == FILE
        setCancelVisible(!isPickFile)
        if (isPickFile) {
            setSubmitText(activity.getString(android.R.string.cancel))
        } else {
            setSubmitText(activity.getString(android.R.string.ok))
        }
    }

    override fun setContentViewAfter(contentView: View) {
        refreshCurrentDirPath(initPath!!)
    }

    override fun onSubmit() {
        if (mode == FILE) {
            LogUtils.verbose("pick file canceled")
        } else if (mode == DIRECTORY) {
            var currentPath = adapter.currentPath
            if (onFilePickListener != null) {
                currentPath = currentPath.replace("//$".toRegex(), "").replace("/$".toRegex(), "") + "/"
                onFilePickListener!!.onFilePicked(currentPath)
            }
        } else {
            val currentPath = adapter.currentPath
            LogUtils.debug("picked directory: $currentPath")
            if (onFilePickListener != null) {
                onFilePickListener!!.onFilePicked(currentPath)
            }
        }
    }

    /**
     * 响应选择器的列表项点击事件
     */
    override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
        val fileItem = adapter.getItem(position)
        if (fileItem.isDirectory) {
            refreshCurrentDirPath(fileItem.path)
        } else {
            val clickPath = fileItem.path
            if (mode == DIRECTORY) {
                LogUtils.warn("not directory: $clickPath")
            } else {
                dismiss()
                LogUtils.debug("picked path: $clickPath")
                if (onFilePickListener != null) {
                    onFilePickListener!!.onFilePicked(clickPath)
                }
            }
        }
    }

    fun setDirectoryPath(path: String) {
        refreshCurrentDirPath(path)
    }

    private fun refreshCurrentDirPath(currentPath: String) {
        if (currentPath == "/") {
            pathAdapter.updatePath("/")
        } else {
            pathAdapter.updatePath(currentPath)
        }
        adapter.loadData(currentPath)
        var adapterCount = adapter.count
        if (adapter.isShowHomeDir) {
            adapterCount--
        }
        if (adapter.isShowUpDir) {
            adapterCount--
        }
        if (adapterCount < 1) {
            LogUtils.verbose(this, "no files, or dir is empty")
            emptyView!!.visibility = View.VISIBLE
            emptyText!!.text = emptyHint
        } else {
            LogUtils.verbose(this, "files or dirs count: $adapterCount")
            emptyView!!.visibility = View.GONE
        }
    }

    fun setOnFilePickListener(listener: OnFilePickListener) {
        this.onFilePickListener = listener
    }

    fun setOnFilePickListener(block: (String) -> Unit) {
        this.onFilePickListener = object : OnFilePickListener {
            override fun onFilePicked(currentPath: String) {
                block(currentPath)
            }
        }
    }

    interface OnFilePickListener {

        fun onFilePicked(currentPath: String)

    }

    companion object {
        const val DIRECTORY = 0
        const val FILE = 1
    }

}
