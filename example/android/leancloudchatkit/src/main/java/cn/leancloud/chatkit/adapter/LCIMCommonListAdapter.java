package cn.leancloud.chatkit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.leancloud.chatkit.utils.LCIMLogUtils;
import cn.leancloud.chatkit.viewholder.LCIMCommonViewHolder;

/**
 * Created by wli on 15/11/23.
 * 单类型 item 的 RecyclerView 对应的 Adapter
 */
public class LCIMCommonListAdapter<T> extends RecyclerView.Adapter<LCIMCommonViewHolder> {
  public enum ListMode {
    SELECT(1),
    SHOW(2),
    SHOW_ACTION(3);
    private int value;
    ListMode(int v) {
      this.value = v;
    }
    public int intValue() {
      return this.value;
    }
  }

  private static HashMap<String, LCIMCommonViewHolder.ViewHolderCreator> creatorHashMap = new HashMap<>();

  private Class<?> vhClass;

  protected List<T> dataList = new ArrayList<T>();

  private ListMode mode = ListMode.SHOW_ACTION;

  public LCIMCommonListAdapter() {
    super();
  }

  public LCIMCommonListAdapter(Class<?> vhClass) {
    this.vhClass = vhClass;
  }

  public void setMode(ListMode m) {
    this.mode = m;
  }

  /**
   * 获取该 Adapter 中存的数据
   *
   * @return
   */
  public List<T> getDataList() {
    return dataList;
  }

  /**
   * 设置数据，会清空以前数据
   *
   * @param datas
   */
  public void setDataList(List<T> datas) {
    dataList.clear();
    if (null != datas) {
      dataList.addAll(datas);
    }
    this.notifyDataSetChanged();
  }

  /**
   * 添加数据，默认在最后插入，以前数据保留
   *
   * @param datas
   */
  public void addDataList(List<T> datas) {
    dataList.addAll(datas);
  }

  @Override
  public LCIMCommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (null == vhClass) {
      try {
        throw new IllegalArgumentException("please use CommonListAdapter(Class<VH> vhClass)");
      } catch (Exception e) {
        LCIMLogUtils.logException(e);
      }
    }

    LCIMCommonViewHolder.ViewHolderCreator<?> creator = null;
    if (creatorHashMap.containsKey(vhClass.getName())) {
      creator = creatorHashMap.get(vhClass.getName());
    } else {
      try {
        creator = (LCIMCommonViewHolder.ViewHolderCreator) vhClass.getField("HOLDER_CREATOR").get(null);
        creatorHashMap.put(vhClass.getName(), creator);
      } catch (IllegalAccessException e) {
        LCIMLogUtils.logException(e);
      } catch (NoSuchFieldException e) {
        LCIMLogUtils.logException(e);
      }
    }
    if (null != creator) {
      return creator.createByViewGroupAndType(parent, viewType, this.mode.intValue());
    } else {
      throw new IllegalArgumentException(vhClass.getName() + " HOLDER_CREATOR should be instantiated");
    }
  }

  @Override
  public void onBindViewHolder(LCIMCommonViewHolder holder, int position) {
    if (position >= 0 && position < dataList.size()) {
      holder.bindData(dataList.get(position));
    }
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }
}