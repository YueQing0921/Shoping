package com.xxx.proj.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.xxx.proj.dto.PageResult;
import com.xxx.proj.dto.Result;
import com.xxx.proj.pojo.TbGoods;
import com.xxx.proj.pojo.TbItem;
import com.xxx.proj.service.GoodsService;
import com.xxx.proj.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
//    @Reference
//    private ItemPageService itemPageService;

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    @Qualifier("updateSolrQueue")
    private Destination updateSolrQueue;

    @Autowired
    @Qualifier("deleteSolrQueue")
    private Destination deleteSolrQueue;

    @Autowired
    @Qualifier("createPageQueue")
    private Destination createPageQueue;

    @Autowired
    @Qualifier("deletePageQueue")
    private Destination deletePageQueue;

    @Reference
    private ItemService itemService;

//    @RequestMapping("/createHtml")
//    public void createHtml(Long goodsId) {
//        itemPageService.createHtml(goodsId);
//    }

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            List<TbItem> itemList = itemService.selectByGoodsId(ids);
            String json = JSON.toJSONString(itemList);
            goodsService.updateStatus(ids, status);
            if ("1".equals(status)) {
                //????????????
                jmsTemplate.send(updateSolrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(json);//TbItem??????
                    }
                });
                jmsTemplate.send(createPageQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(ids);//goodsId??????
                    }
                });

            } else {
                //????????????
                jmsTemplate.send(deleteSolrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(json);//TbItem??????
                    }
                });
                jmsTemplate.send(deletePageQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(ids);//goodsId??????
                    }
                });
            }
            return new Result(true, "????????????");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "????????????");
        }
    }
//    @RequestMapping("/updateStatus")
//    public Result updateStatus(Long[] ids, String status) {
//        try {
//            goodsService.updateStatus(ids, status);
//            //??????????????????
//            if ("1".equals(status)) {
//                for (Long id : ids) {
//                    itemPageService.createHtml(id);
//                }
//            }
//            return new Result(true, "????????????");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new Result(false, "????????????");
//        }
//    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * ??????
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbGoods goods) {
        try {
            goodsService.add(goods);
            return new Result(true, "????????????");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "????????????");
        }
    }

    /**
     * ??????
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbGoods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "????????????");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "????????????");
        }
    }

    /**
     * ????????????
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TbGoods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * ????????????
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            return new Result(true, "????????????");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "????????????");
        }
    }

    /**
     * ??????+??????
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

}
