## 大分类

### 小分类

1. 测试提交订单流程
    - **Objective**：测试订单可以提交，不涉及撮合（几句话描述目标，这个信息是最重要的，要写详细些。这样及时别人不了解下线的数据，也可以自己从新做一遍。）
    - **测试设置**：
        1. 设置账号有1000个LRC，下一个卖10个LRC的单，价格是...
        1. 下订单
    - **结果验证**：
        1. **读取我的订单**：通过getOrders应该看到该订单作为第一个返回，其中的值应该是...
        1. **读取市场深度**：卖单深度应该是...，买单应该是...
        1. **读取我的成交**: 应该为空
        1. **读取市场成交**： 应该为空
        1. **读取我的账号**: LRC 可用余额应为...
    - **状态**: Planned/Tested/Failing/Done
    - **拥有者**: 亚东
    - **其他信息**：NA
    
    
1. 测试提交订单流程
    - **Objective**：测试订单可以提交，不涉及撮合（几句话描述目标，这个信息是最重要的，要写详细些。这样及时别人不了解下线的数据，也可以自己从新做一遍。）
    - **测试设置**：
        1. 设置账号有1000个LRC，下一个卖10个LRC的单，价格是...
        1. 下订单
    - **结果验证**：
        1. **读取我的订单**：通过getOrders应该看到该订单作为第一个返回，其中的值应该是...
        1. **读取市场深度**：卖单深度应该是...，买单应该是...
        1. **读取我的成交**: 应该为空
        1. **读取市场成交**： 应该为空
        1. **读取我的账号**: LRC 可用余额应为...
    - **状态**: Planned/Tested/Failing/Done
    - **拥有者**: 亚东
    - **其他信息**：NA
