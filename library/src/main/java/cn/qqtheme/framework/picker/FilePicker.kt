package cn.qqtheme.framework.picker

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.IntDef
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView

import com.common.*
import com.library.R

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import cn.qqtheme.framework.adapter.FileAdapter
import cn.qqtheme.framework.adapter.PathAdapter
import cn.qqtheme.framework.entity.FileItem
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
class FilePicker
/**
 * @see .FILE
 *
 * @see .DIRECTORY
 */
(activity: Activity, @param:Mode private val mode: Int) : ConfirmPopup<LinearLayout>(activity), AdapterView.OnItemClickListener {

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

    //    @Override
    //    @NonNull
    //    protected LinearLayout makeCenterView() {
    //        LinearLayout rootLayout = new LinearLayout(activity);
    //        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    //        rootLayout.setOrientation(LinearLayout.HORIZONTAL);
    //        rootLayout.setBackgroundColor(Color.WHITE);
    //
    //        LinearLayout side = new LinearLayout(activity);
    //        side.setLayoutParams(new LinearLayout.LayoutParams(200, MATCH_PARENT));
    //        side.setOrientation(LinearLayout.HORIZONTAL);
    //        side.setBackgroundColor(Color.BLACK);
    //        rootLayout.addView(side);
    //
    //        ListView listView = new ListView(activity);
    //        listView.setBackgroundColor(Color.WHITE);
    //        listView.setDivider(new ColorDrawable(0xFFDDDDDD));
    //        listView.setDividerHeight(1);
    //        listView.setCacheColorHint(Color.TRANSPARENT);
    //        LinearLayout.LayoutParams listViewLayoutParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT);
    //        listViewLayoutParams.weight = 1;
    //        listView.setLayoutParams(listViewLayoutParams);
    //        listView.setAdapter(adapter);
    //        listView.setOnItemClickListener(this);
    //        rootLayout.addView(listView);
    //
    //        emptyText = new TextView(activity);
    //        LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(0, MATCH_PARENT);
    //        txtParams.weight = 1;
    //        emptyText.setLayoutParams(txtParams);
    //        emptyText.setGravity(Gravity.CENTER);
    //        emptyText.setVisibility(View.GONE);
    //        emptyText.setTextColor(Color.BLACK);
    //        emptyText.setBackgroundColor(Color.GREEN);
    //        if(emptyView == null){
    //            emptyView = emptyText;
    //        }
    //        rootLayout.addView(emptyView);
    //
    //        return rootLayout;
    //    }

    override fun makeCenterView(): View {
        val rootLayout = ConstraintLayout(activity)
        rootLayout.id = View.generateViewId()
        rootLayout.layoutParams = ConstraintLayout.LayoutParams(BasicPopup.MATCH_PARENT, BasicPopup.MATCH_PARENT)
        rootLayout.setBackgroundColor(0x11000000)

        val side = LinearLayout(activity)
        val sideParams = ConstraintLayout.LayoutParams(200, BasicPopup.MATCH_PARENT)
        sideParams.setMargins(0, 16, 16, 16)
        sideParams.topToTop = ConstraintSet.PARENT_ID
        sideParams.startToStart = ConstraintSet.PARENT_ID
        sideParams.bottomToBottom = ConstraintSet.PARENT_ID
        side.layoutParams = sideParams
        side.gravity = Gravity.CENTER_HORIZONTAL
        side.orientation = LinearLayout.VERTICAL
        side.id = View.generateViewId()
        val list = activity.storageList()
        if (list != null) {
            for (i in list.indices) {
                val path = list[i]
                val text = TextView(activity)
                val params = LinearLayout.LayoutParams(BasicPopup.MATCH_PARENT, BasicPopup.WRAP_CONTENT)
                params.topMargin = 4
                params.bottomMargin = 4
                text.setTextColor(AppCompatResources.getColorStateList(activity, R.color.storage_text_color))
                text.text = (i + 1).toString() + ""
                text.gravity = Gravity.CENTER
                val drawable = AppCompatResources.getDrawable(activity, R.drawable.storage)
                if (drawable != null) {
                    drawable.setBounds(0, 0, 160, 160)
                    text.setCompoundDrawables(null, drawable, null, null)
                }
                text.layoutParams = params
                text.setOnClickListener { setDirectoryPath(path) }
                side.addView(text)
            }
        }
        rootLayout.addView(side)

        val listView = ListView(activity)
        listView.id = View.generateViewId()
        listView.setBackgroundColor(Color.WHITE)
        listView.divider = ColorDrawable(-0x222223)
        listView.dividerHeight = 1
        listView.cacheColorHint = Color.TRANSPARENT
        val listViewLayoutParams = ConstraintLayout.LayoutParams(0, BasicPopup.MATCH_PARENT)
        listViewLayoutParams.setMargins(16, 16, 16, 16)
        listViewLayoutParams.topToTop = ConstraintSet.PARENT_ID
        listViewLayoutParams.bottomToBottom = ConstraintSet.PARENT_ID
        listViewLayoutParams.startToEnd = side.id
        listViewLayoutParams.endToEnd = ConstraintSet.PARENT_ID
        listView.layoutParams = listViewLayoutParams
        listView.adapter = adapter
        listView.onItemClickListener = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.elevation = 10f
        }
        rootLayout.addView(listView)

        emptyText = TextView(activity)
        val txtParams = ConstraintLayout.LayoutParams(0, BasicPopup.MATCH_PARENT)
        txtParams.setMargins(16, 16, 16, 16)
        emptyText!!.gravity = Gravity.CENTER
        emptyText!!.visibility = View.GONE
        emptyText!!.setTextColor(Color.BLACK)
        emptyText!!.setBackgroundColor(Color.WHITE)
        if (emptyView == null) {
            emptyView = emptyText
        }
        emptyView!!.layoutParams = txtParams
        txtParams.topToTop = ConstraintSet.PARENT_ID
        txtParams.bottomToBottom = ConstraintSet.PARENT_ID
        txtParams.startToEnd = side.id
        txtParams.endToEnd = ConstraintSet.PARENT_ID
        emptyView!!.id = View.generateViewId()
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

    override fun dismiss() {
        super.dismiss()
        //adapter.recycleData();
        //pathAdapter.recycleData();
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

    fun setOnFilePickListener(block : (String) -> Unit) {
        this.onFilePickListener = object : OnFilePickListener{
            override fun onFilePicked(currentPath: String) {
                block(currentPath)
            }
        }
    }

    interface OnFilePickListener {

        fun onFilePicked(currentPath: String)

    }

    companion object {
        val DIRECTORY = 0
        val FILE = 1
    }

}
