package com.atguigu.gmall.item.Demo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author ZQ
 * @create 2019-11-11 16:37
 * <p>
 * 异步编排测试
 */
public class CompletableFutureDemo {
    public static void main(String[] args) {
        List<CompletableFuture<String>> completableFutures = Arrays.asList(CompletableFuture.completedFuture("hello"), CompletableFuture.completedFuture("java0615"));
        completableFutures.toArray();
        CompletableFuture<Void> future = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{}));
        future.whenComplete((t, u) -> {
            completableFutures.stream().forEach(completableFuture -> {
                try {
                    System.out.println(completableFuture.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });


//        CompletableFuture<String> fulture = CompletableFuture.supplyAsync(() -> {
//            System.out.println("supplyAsync");
////            int i = 1/0;
//            return "supplyAsync";
//        }).thenApply(t -> {
//            System.out.println("thenApply........");
//            System.out.println("t..................." + t);
//            return "thenApply";
//        }).whenComplete((t, u) -> {
//            System.out.println("whenComplete........");
//            System.out.println("t..................." + t);
//            System.out.println("u..................." + u);
//        }).exceptionally(t -> {
//            System.out.println("exceptionally........");
//            System.out.println("t..................." + t);
//            return "exceptionally";
//        }).handle((t, u) -> {
//            System.out.println("handle........");
//            System.out.println("t..................." + t);//上个流程返回的结果集
//            System.out.println("u..................." + u);//异常信息
//            return "handle";
//        }).thenCombine(CompletableFuture.completedFuture("CompletableFuture"),(t,u)->{
//            System.out.println("t............."+t);
//            System.out.println("u............."+u);
//            System.out.println("两个线程完成后新线程执行任务");
//            return  "thenCombine";
//        }).handle((t,u)->{
//            System.out.println("handle........");
//            System.out.println("t............."+t);
//            System.out.println("u............."+u);
//           return  "ccccccccccc" ;
//        });

    }
}
