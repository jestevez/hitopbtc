package com.hitop.controller;

/*
 *  Copyright 2020 Nick Avlonitis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */    

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;
import com.hitop.entity.PurchaseOrder;
import com.hitop.repository.OrderRepository;
import com.hitop.service.CoinReceivedService;
import com.hitop.service.OrderServiceImpl;
import com.hitop.service.QRCodeService;
import com.hitop.service.RateService;
import com.hitop.service.WalletService;

@Controller
@RequestMapping(path="/")
public class MainController implements ReceiptListener {
  final Logger logger = LoggerFactory.getLogger(MainController.class);
  
  final static long SSE_EMITTER_TIMEOUT = 1200000l;

  @Autowired
  private OrderRepository orderRepository;
  
  @Autowired
  private WalletService walletService;
  
  @Autowired
  private CoinReceivedService coinReceivedService;
  
  @Autowired
  private QRCodeService qrCodeService;
  
  @Autowired
  private RateService rateService;
  
  @Autowired
  private OrderServiceImpl orderServiceImpl;
  
  @Autowired
  private PurchaseOrder purchaseOrder;
  
  private SseEmitter emitter;
  
  @GetMapping("/orderdetails")
  public String getOrderDetails(final Model model) throws Exception {
    walletService.addCoinsReceivedEventListener(coinReceivedService);
    model.addAttribute("order", this.purchaseOrder);
    return "orderdetails";
  }

  @PostMapping("/ordersubmit")
  public String displayQR(final PurchaseOrder order,
      final BindingResult result, final Model model) throws Exception {
    // TODO: fix below two lines, seems hacky
    this.purchaseOrder = order;
    this.purchaseOrder.setBtcPublicKey(qrCodeService.getQRCodeUrl(walletService.getSendToAddress()));
    logger.debug(this.purchaseOrder.getName());
    // TODO 90: uncomment when errors are implemented
//    if (result.hasErrors()) {
//      return "orderdetails";
//    }
    
    model.addAttribute(this.purchaseOrder);
    // TODO 70 : call appropriate ordersubmit.html file based on stub/test/etc
    return "ordersubmit";
  }
  
  @GetMapping("/receipt-sse")
  public SseEmitter setupSSEEmitter() {
    this.emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
    return this.emitter;
  }

  public PurchaseOrder displayReceiptSse() {
    PurchaseOrder order = orderServiceImpl.save(this.purchaseOrder);
    // TODO: is below newSingleThreadExecutor() the correct method to use?
    ExecutorService executor = Executors.newSingleThreadExecutor(); // Executors.newCachedThreadPool();
    executor.execute(() -> {
      try {
        SseEventBuilder event = SseEmitter.event()
            .data("{\"name\":\"" + order.getName());
        //  .data("{\"email\":\"" + order.getEmail());
        //   etc
        this.emitter.send(event);
        this.emitter.complete();
      } catch (Exception e) {
        logger.info("##### EMITTER EXCEPTION: " + e.toString());
        this.emitter.completeWithError(e);
      }
    });
    executor.shutdown();
    return order;
  }

  //TODO 50 keep this but wrap it in security so only admin can call it
  @GetMapping(path="/allordershitop")
  public @ResponseBody Iterable<PurchaseOrder> getAllOrders() {
    return orderRepository.findAll();
  }
}
