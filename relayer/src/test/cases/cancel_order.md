
## 取消订单

按订单的几个状态：[STATUS_PENDING](#pending)，[STATUS_PARTIALLY_FILLED](#partially)， [STATUS_ONCHAIN_CANCELLED_BY_USER](#onchain_cancelled)取消订单。
有效状态的订单可以被取消为各种状态，onchain cancel后不能再被软取消，在pending时同时验证按orderHash，owner，owner-marketPair, cutoffEvent,
ordersCancelledOnChainEvent取消，其他状态只验证按orderHash取消。最后是一些[需要验证的情况](#to_check)

 ###  <a name="pending"></a> STATUS_PENDING状态订单的取消

 1. **测试按orderHash取消订单流程**
    - **Objective**：测试按orderHash可以正确取消订单，不存在的orderHash会返回错误码

    - **测试设置**：

        1. 设置账号有1000个LRC，授权充足
        1. 初始获取账号余额等
        1. 在LRC-WETH市场下两个LRC的卖单，其中价格是0.01WETH，orderHash：order_hash1，另一个是PENDING_ACTIVE，orderHash：order_hash1
        1. 发送CancelOrder请求，请求参数orderHash：order_hash1，status：STATUS_SOFT_CANCELLED_BY_USER
        	- 结果验证：
          	    1. **返回结果**：ERR_NONE
          	    1. **读取我的订单**：通过getOrders应该看到该订单，其中的status应该为STATUS_SOFT_CANCELLED_BY_USER
          	    1. **读取市场深度**：卖单深度应该为订单二的卖出
          	    1. **读取我的成交**: 应该为空
          	    1. **读取市场成交**： 应该为空
          	    1. **读取我的账号**: 账户余额应该等于初始值
        1. 发送CancelOrder请求，请求参数orderHash：order_hash2，status：STATUS_SOFT_CANCELLED_BY_USER
        	- 结果验证：
          	    1. **返回结果**：ERR_NONE
          	    1. **读取我的订单**：通过getOrders应该看到该订单，其中的status应该为STATUS_SOFT_CANCELLED_BY_USER
          	    1. **读取市场深度**：卖单深度应该为空
          	    1. **读取我的成交**: 应该为空
          	    1. **读取市场成交**： 应该为空
          	    1. **读取我的账号**: 账户余额应该等于初始值
        1. 再次发送CancelOrder请求，请求参数order_hash2，STATUS_SOFT_CANCELLED_BY_USER
        	- 结果验证：
        		1. **返回结果**：ERR_ORDER_NOT_EXIST

    - **状态**: Planned

    - **拥有者**: 杜永丰, 于红雨

    - **其他信息**：NA

1. **测试按owner取消订单流程**

    - **Objective**：测试可以按owner正确取消订单，owner不存在订单的会返回错误码

    - **测试设置**：

        1. 设置A1账号有1000个LRC，1000个GTO，授权充足
        1. 在LRC-WETH市场下一个卖10个LRC的单，价格是0.01WETH，返回orderHash：O1
        1. 在GTO-WETH市场下一个卖10个GTO的单，价格是0.001WETH，返回orderHash：O2
        1. 发送CancelOrder请求，请求参数owner：A1
            - 结果验证：
          	    1. **返回结果**：ERR_NONE
          	    1. **读取我的订单**：通过getOrders应该看到该订单，其中的status应该为STATUS_SOFT_CANCELLED_BY_USER
          	    1. **读取市场深度**：2个市场深度都为0
          	    1. **读取我的成交**: 应该为空
          	    1. **读取市场成交**： 应该为空
          	    1. **读取我的账号**: LRC 可用余额应为1000，GTO可用余额应为1000

        1. 发送CancelOrder请求，请求参数owner：A2，status：STATUS_SOFT_CANCELLED_BY_USER
        	- 结果验证：
        		1. **返回结果**：ERR_ORDER_NOT_EXIST

    - **状态**: Tested

    - **拥有者**: 杜永丰, 于红雨

    - **其他信息**：NA
    
1. **测试按owner-marketPair取消订单流程**
    - **Objective**：测试按owner和marketPair可以正确取消订单，不存在订单的会返回错误码

    - **测试设置**：

        1. 设置A1账号有1000个LRC，授权充足
        1. 在LRC-WETH市场下一个卖10个LRC的单，价格是0.01WETH，返回orderHash：order_hash1
        1. 在GTO-WETH市场下一个卖单，orderhash：order_hash2
        1. 发送CancelOrder请求，请求参数owner：A1，marketPair：LRC-WETH
        	- 结果验证：
          	    1. **返回结果**：ERR_NONE
          	    1. **读取我的订单**：通过getOrders应该看到order_hash1，其中的status应该为STATUS_SOFT_CANCELLED_BY_USER,
          	    order_hash2 的状态为STATUS_PENDING
          	    1. **读取市场深度**：LRC-WETH市场深度为0，GTO-WETH市场深度为order_hash2的卖出量
          	    1. **读取我的成交**: 应该为空
          	    1. **读取市场成交**： 应该为空
          	    1. **读取我的账号**: GTO的可用余额和授权为减掉order_hash2的值

        1. 再次发送CancelOrder请求，请求参数owner：A1，marketPair：LRC-WETH
        	- 结果验证：
        		1. **返回结果**：ERR_ORDER_NOT_EXIST

    - **状态**: Tested

    - **拥有者**: 杜永丰, 于红雨

    - **其他信息**：NA
    
1. **测试CutoffEvent取消owner订单流程**

    - **Objective**：测试OwnerCutoff事件，会取消cutoff时间之前的订单，但是对cutoff时间之后的订单无影响，并且，如果再次提交订单时，仍会只允许提交cutoff时间之后的订单

    - **测试设置**：

        1. 设置A1账号有1000个LRC，授权充足
        1. 在LRC-WETH市场提交两个卖单order_hash1和order_hash2，validsince分别为当前时间-100s和当前时间-10s
        1. 在GTO-WETH市场提交一个卖单，validsince为当前时间-100s
        1. 发送CutoffEvent，参数broker=A2, owner=A2，cutoff=当前时间-50s
            - 结果验证：
                1. **读取我的订单**：通过getOrders应该看到三个订单，status应该为STATUS_PENDING
                1. **读取市场深度**：两个市场分别有10个卖单深度
                1. **读取我的成交**: 无
                1. **读取市场成交**：无
                1. **读取我的账号**: LRC 可用余额应为1000-order_hash2,LRC 可用授权应为1000-order_hash2
        1. 发送CutoffEvent，参数broker=A1, owner=A1，cutoff=当前时间
            - 结果验证：
                1. **读取我的订单**：通过getOrders应该看到三个订单，order_hash2状态为STATUS_PENDING，其余订单状态为STATUS_ONCHAIN_CANCELLED_BY_USER，
                1. **读取市场深度**：LRC-WETH市场深度为order_hash2的卖单
                1. **读取我的成交**: 无
                1. **读取市场成交**：无
                1. **读取我的账号**: LRC 可用余额应为1000-order_hash2,GTO 可用授权应为1000-order_hash2
        1. 继续提交两个GTO-WETH卖单order_hash5,order_hash6，生效时间分别为：当前时间-100s和当前时间，
            - 结果验证：
                1. **读取我的订单**：通过getOrders应该看到四个订单，order_hash6状态为STATUS_PENDING，其余订单状态为STATUS_ONCHAIN_CANCELLED_BY_USER，
                1. **读取市场深度**：LRC-WETH市场深度为order_hash2的卖单, GTO-WETH市场的深度为order_hash6的卖单
                1. **读取我的成交**: 无
                1. **读取市场成交**：无
                1. **读取我的账号**: LRC 可用余额应为1000-order_hash2,LRC 可用授权应为1000-order_hash2
                                   GTO 可用余额应为1000-order_hash6,GTO 可用授权应为1000-order_hash6
        
    - **状态**: Tested

    - **拥有者**: 杜永丰, 于红雨

    - **其他信息**：NA
    
1. **测试CutoffEvent取消owner-market订单流程**

    - **Objective**：收到CutoffEvent可以正确取消订单

    - **测试设置**：

        1. 设置A1账号有1000个LRC，授权充足
        1. 在LRC-WETH市场提交两个卖单order_hash1和order_hash2，validSince分比为now-1000和now
        1. 在GTO-WETH市场一个卖单order_hash3,validSince为now-1000
        1. 发送CutoffEvent，参数broker=A1, owner=A1, marketHash=LRC-WETH
            - 结果验证：
                1. **读取我的订单**：getOrders包含三个订单，order_hash1状态为STATUS_ONCHAIN_CANCELLED_BY_USER，其余的status应该为STATUS_PENDING
                1. **读取市场深度**：两个市场分别为对应的两个订单的市场深度
                1. **读取我的成交**: 无
                1. **读取市场成交**：无
                1. **读取我的账号**: LRC 可用余额应为1000 - order_hash2,LRC可用授权应为1000 - order_hash2
                                   GTO 可用余额应为1000 - order_hash3,GTO可用授权应为1000 - order_hash3
                                   
        1. 在GTO-WETH市场提交一个卖单order_hash4,validSince为now-1000
        1. 在LRC-WETH市场再提交两个卖单order_hash5和order_hash6， validSince分比为now-1000和now
            - 结果验证：
                1. **读取我的订单**：getOrders包含五个订单，order_hash1状态为STATUS_ONCHAIN_CANCELLED_BY_USER，其余的status应该为STATUS_PENDING
                1. **读取市场深度**：两个市场分别为对应四个订单的市场深度
                1. **读取我的成交**: 无
                1. **读取市场成交**：无
                1. **读取我的账号**: LRC 可用余额应为1000 - order_hash2 - order_hash6,LRC可用授权应为1000 - order_hash2 - order_hash6
                                   GTO 可用余额应为1000 - order_hash3 - order_hash4,GTO可用授权应为1000 - order_hash3 - order_hash4      

    - **状态**: Tested

    - **拥有者**: 杜永丰, 于红雨

    - **其他信息**：NA
    
1. **测试OrdersCancelledOnChainEvent取消订单流程**

    - **Objective**：收到OrdersCancelledOnChainEvent可以正确取消订单

    - **测试设置**：

        1. 设置A1账号有1000个LRC，授权充足
        1. 在LRC-WETH市场下一个卖10个LRC的单，价格是0.01WETH，返回orderHash：O1
        1. 在GTO-WETH市场下一个卖10个GTO的单，价格是0.001WETH，返回orderHash：O2
        1. 发送OrdersCancelledOnChainEvent，参数Seq(O1, O2)
            - 结果验证：
                1. **读取我的订单**：通过getOrders应该看到该订单，其中的status应该为STATUS_ONCHAIN_CANCELLED_BY_USER
                1. **读取市场深度**：2个市场卖单深度应该变为0
                1. **读取我的成交**: 无
                1. **读取市场成交**：无
                1. **读取我的账号**: LRC 可用余额应为1000,GTO 可用余额应为1000

    - **状态**: Tested

    - **拥有者**: 杜永丰，于红雨

    - **其他信息**：NA
    
---

###  <a name="partially"></a> STATUS_PARTIALLY_FILLED状态订单的取消

1. **测试按orderHash取消订单流程**

    - **Objective**：测试按orderHash可以正确取消订单，不存在的orderHash会返回错误码

    - **测试设置**：

        1. 设置账号有1000个LRC，授权充足
        1. 在LRC-WETH市场下一个卖100个LRC的单，价格是0.01WETH，返回orderHash：O1
        1. 发送OrderFilledEvent事件，模拟成交1个LRC
            - 结果验证：
                1. **读取我的订单**：通过getOrders查询status应该为STATUS_PARTIALLY_FILLED
                1. **读取市场深度**：卖单深度应该变为99
                1. **读取我的成交**: 有1LRC卖单成交
                1. **读取市场成交**： 有1LRC卖单成交
                1. **读取我的账号**: LRC 余额应为999, 可用余额900

        1. 发送CancelOrder请求，请求参数orderHash：O1，status：STATUS_SOFT_CANCELLED_BY_USER
            - 结果验证：
          	    1. **返回结果**：ERR_NONE
          	    1. **读取我的订单**：通过getOrders查询status应该为STATUS_SOFT_CANCELLED_BY_USER
          	    1. **读取市场深度**：卖单深度应该变为0
          	    1. **读取我的成交**: 有1LRC卖单成交
          	    1. **读取市场成交**：有1LRC卖单成交
          	    1. **读取我的账号**: LRC 余额应为999, 可用余额999

        1. 发送CancelOrder请求，请求参数O2，STATUS_SOFT_CANCELLED_BY_USER

        	- 结果验证：
        		1. **返回结果**：ERR_ORDER_NOT_EXIST

    - **状态**: Tested

    - **拥有者**: 杜永丰，于红雨

    - **其他信息**：NA
    
---
###  <a name="onchain_cancelled"></a> STATUS_ONCHAIN_CANCELLED_BY_USER状态订单的取消

1. **测试按orderHash取消订单流程**
    - **Objective**：测试按orderHash不应该更改订单状态

    - **测试设置**：
        1. 设置账号有1000个LRC，授权充足
        1. 在LRC-WETH市场下一个卖100个LRC的单，价格是0.01WETH，返回orderHash：O1
        1. 发送OrdersCancelledOnChainEvent事件，传入orderHash：O1
            - 结果验证：
                1. **读取我的订单**：通过getOrders查看订单status应该为STATUS_ONCHAIN_CANCELLED_BY_USER
                1. **读取市场深度**：卖单深度应该变为0
                1. **读取我的成交**:  无
                1. **读取市场成交**：无
                1. **读取我的账号**: LRC 可用余额应为1000

        1. 发送CancelOrder请求，请求参数orderHash：O1，status：STATUS_SOFT_CANCELLED_BY_USER
            - 结果验证：
                1. **返回结果**：应该返回错误，目前代码未处理
        1. 发送CancelOrder请求，请求参数O2，STATUS_SOFT_CANCELLED_BY_USER
        	- 结果验证：
        		1. **返回结果**：ERR_ORDER_NOT_EXIST

    - **状态**: Tested

    - **拥有者**: 杜永丰，于红雨

    - **其他信息**：NA

---
###  <a name="to_check"></a> 一些需要验证的情况

1. **测试按已经取消的订单再收到OrderFilledEvent处理**

    - **Objective**：测试如果OrderCancel和OrderFill事件同时出发，顺序对系统的影响，测试OrderCancel先收到的情况

    - **测试设置**：

        1. 设置账号有1000个LRC，授权充足
        1. 在LRC-WETH市场下一个卖单
        1. 发送CancelOrder.Req取消该订单
            - 结果验证：
                1. **读取我的订单**：通过getOrders查询status应该为STATUS_SOFT_CANCELLED_BY_USER
                1. **读取市场深度**：卖单深度应该变为0
                1. **读取我的成交**: 无
                1. **读取市场成交**： 无
                1. **读取我的账号**: LRC 余额应为1000, 可用余额1000
        1. 发送OrderFilledEvent事件，模拟成交1个LRC
            - 结果验证：
                1. **读取我的订单**：通过getOrders查询status应该为STATUS_SOFT_CANCELLED_BY_USER，但是outstanding等金额需要更改
                1. **读取市场深度**：卖单深度应该变为0
                1. **读取我的成交**: 有1LRC卖单成交
                1. **读取市场成交**： 有1LRC卖单成交
                1. **读取我的账号**: LRC 余额应为999, 可用余额900

    - **状态**: Tested

    - **拥有者**: 杜永丰，于红雨

    - **其他信息**：NA
    
1. **测试已经取消的订单对对订单有效金额的影响**

    - **Objective**：测试取消一个订单对另一个订单的影响

    - **测试设置**：

        1. 设置账号有100个LRC，授权充足
        1. 在LRC-WETH市场下一个卖80个LRC的单，价格是0.01WETH，返回orderHash：O1
            - 结果验证：
                1. **读取我的订单**：通过getOrders查询O1订单status应该为STATUS_PENDING
                1. **读取市场深度**：卖单深度应该变为80
                1. **读取我的成交**: 无
                1. **读取市场成交**： 无
                1. **读取我的账号**: LRC 余额应为100, 可用余额20
        1. 在LRC-WETH市场下一个卖80个LRC的单，价格是0.01WETH，返回orderHash：O2
            - 结果验证：
                1. **读取我的订单**：通过getOrders查询O2订单status应该为STATUS_PENDING
                1. **读取市场深度**：卖单深度应该变为100
                1. **读取我的成交**: 无
                1. **读取市场成交**： 无
                1. **读取我的账号**: LRC 余额应为100, 可用余额0
        1. 发送CancelOrder.Req取消订单：O1
            - 结果验证：
                1. **读取我的订单**：通过getOrders查询O1订单status应该为STATUS_SOFT_CANCELLED_BY_USER
                1. **读取市场深度**：卖单深度应该变为80
                1. **读取我的成交**: 无
                1. **读取市场成交**： 无
                1. **读取我的账号**: LRC 余额应为100, 可用余额20

    - **状态**: Tested

    - **拥有者**: 杜永丰，于红雨

    - **其他信息**：NA