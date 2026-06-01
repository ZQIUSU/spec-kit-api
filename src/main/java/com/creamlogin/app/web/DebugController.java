package com.creamlogin.app.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仅供 JVM 学习使用的调试接口——故意制造内存泄漏，方便观察 GC / 触发 OOM / 生成堆转储。
 *
 * <p><b>切勿用于生产</b>。学习结束后请删除本类，并从 SecurityConfig 移除 /api/debug/** 的放行。
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {

  /** 静态列表 = GC Root 可达，里面的对象永远不会被回收 → 这就是典型的内存泄漏。 */
  private static final List<byte[]> LEAK = new ArrayList<>();

  /** 每调一次，往泄漏列表里塞一块内存（默认 4MB）。 */
  @GetMapping("/leak")
  public Map<String, Object> leak(@RequestParam(defaultValue = "4") int mb) {
    LEAK.add(new byte[mb * 1024 * 1024]);
    return heapInfo();
  }

  /** 清空泄漏列表（下一次 GC 即可回收）。 */
  @GetMapping("/leak/reset")
  public Map<String, Object> reset() {
    int n = LEAK.size();
    LEAK.clear();
    Map<String, Object> m = heapInfo();
    m.put("cleared", n);
    return m;
  }

  private Map<String, Object> heapInfo() {
    Runtime rt = Runtime.getRuntime();
    long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
    long maxMB = rt.maxMemory() / 1024 / 1024;
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("leakBlocks", LEAK.size());
    m.put("leakedMB", LEAK.size() * 4L);
    m.put("heapUsedMB", usedMB);
    m.put("heapMaxMB", maxMB);
    return m;
  }
}
