const express = require('express');
const axios = require('axios');
const cors = require('cors');
const { format } = require('date-fns');
const { toZonedTime } = require('date-fns-tz');

const app = express();
const PORT = 3001;

// 中间件
app.use(cors());
app.use(express.json());

// 12306 API配置
const API_BASE = 'https://kyfw.12306.cn';
const WEB_URL = 'https://www.12306.cn/index/';

// Cookie解析函数
function parseCookies(cookies) {
  const cookieRecord = {};
  if (!cookies) return cookieRecord;
  
  cookies.forEach((cookie) => {
    const keyValuePart = cookie.split(';')[0];
    const [key, value] = keyValuePart.split('=');
    if (key && value) {
      cookieRecord[key.trim()] = value.trim();
    }
  });
  return cookieRecord;
}

// Cookie格式化函数
function formatCookies(cookies) {
  return Object.entries(cookies)
    .map(([key, value]) => `${key}=${value}`)
    .join('; ');
}

// 获取Cookie
async function getCookie(url) {
  try {
    console.log('开始获取Cookie，URL:', url);
    const response = await axios.get(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8',
        'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        'Accept-Encoding': 'gzip, deflate, br',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1'
      },
      timeout: 15000,
      maxRedirects: 5 // 允许重定向
    });
    
    console.log('HTTP响应状态码:', response.status);
    const setCookieHeader = response.headers['set-cookie'];
    
    if (setCookieHeader) {
      const cookies = parseCookies(setCookieHeader);
      console.log('解析后的Cookie数量:', Object.keys(cookies).length);
      return cookies;
    }
    return {};
  } catch (error) {
    console.error('获取Cookie失败:', error.message);
    return {};
  }
}

// 12306 API请求
async function make12306Request(url, params = {}, headers = {}) {
  try {
    const response = await axios.get(url, {
      params: params,
      headers: headers,
      timeout: 30000
    });
    return response.data;
  } catch (error) {
    console.error('12306 API请求失败:', error.message);
    return null;
  }
}

// 检查日期是否有效
function checkDate(date) {
  const today = new Date();
  const queryDate = new Date(date);
  return queryDate >= today;
}

// API路由

// 获取当前日期
app.get('/api/current-date', (req, res) => {
  try {
    const timeZone = 'Asia/Shanghai';
    const nowInShanghai = toZonedTime(new Date(), timeZone);
    const formattedDate = format(nowInShanghai, 'yyyy-MM-dd');
    res.json({ date: formattedDate });
  } catch (error) {
    console.error('获取当前日期失败:', error);
    res.status(500).json({ error: '获取当前日期失败' });
  }
});

// 查询车票
app.get('/api/tickets', async (req, res) => {
  try {
    const { date, fromStation, toStation, trainFilterFlags = '' } = req.query;
    
    console.log('查询车票信息:', { date, fromStation, toStation, trainFilterFlags });
    
    // 参数验证
    if (!date || !fromStation || !toStation) {
      return res.status(400).json({ error: '缺少必要参数' });
    }
    
    if (!checkDate(date)) {
      return res.status(400).json({ error: '日期不能早于今天' });
    }
    
    // 获取Cookie
    const cookies = await getCookie(API_BASE);
    if (Object.keys(cookies).length === 0) {
      console.warn('未获取到Cookie，尝试继续请求');
    }
    
    // 构建查询参数
    const queryParams = {
      'leftTicketDTO.train_date': date,
      'leftTicketDTO.from_station': fromStation,
      'leftTicketDTO.to_station': toStation,
      'purpose_codes': 'ADULT'
    };
    
    // 构建请求头
    const requestHeaders = {
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
      'Accept': 'application/json, text/javascript, */*; q=0.01',
      'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
      'Referer': 'https://kyfw.12306.cn/otn/leftTicket/init',
      'X-Requested-With': 'XMLHttpRequest'
    };
    
    if (Object.keys(cookies).length > 0) {
      requestHeaders['Cookie'] = formatCookies(cookies);
    }
    
    // 发送请求
    const queryUrl = `${API_BASE}/otn/leftTicket/query`;
    console.log('发送API请求:', queryUrl);
    
    const response = await make12306Request(queryUrl, queryParams, requestHeaders);
    
    if (!response) {
      return res.status(500).json({ error: '查询车票信息失败' });
    }
    
    console.log('API响应状态:', response.httpstatus);
    
    if (response.httpstatus !== 200) {
      return res.status(400).json({ 
        error: '请求失败', 
        message: response.messages || '未知错误' 
      });
    }
    
    if (!response.data || !response.data.result) {
      return res.json({ tickets: [], message: '没有找到符合条件的车票信息' });
    }
    
    // 解析车票数据
    const tickets = parseTicketsData(response.data.result, response.data.map);
    
    // 根据车型过滤
    let filteredTickets = tickets;
    if (trainFilterFlags) {
      filteredTickets = filterTicketsByTrainTypes(tickets, trainFilterFlags);
    }
    
    console.log(`查询完成，返回 ${filteredTickets.length} 条车票信息`);
    res.json({ tickets: filteredTickets });
    
  } catch (error) {
    console.error('查询车票异常:', error);
    res.status(500).json({ error: '查询车票信息出错', message: error.message });
  }
});

// 解析车票数据
function parseTicketsData(resultArray, stationMap) {
  const tickets = [];
  
  for (const ticketString of resultArray) {
    const parts = ticketString.split('|');
    if (parts.length < 30) continue;
    
    try {
      const ticket = {
        trainNo: parts[2],
        startTrainCode: parts[3],
        startTime: parts[8],
        arriveTime: parts[9],
        lishi: parts[10],
        fromStation: getStationName(stationMap, parts[6]),
        toStation: getStationName(stationMap, parts[7]),
        fromStationTelecode: parts[6],
        toStationTelecode: parts[7],
        prices: extractPrices(parts),
        dwFlag: extractDWFlags(parts[46] || '')
      };
      
      tickets.push(ticket);
    } catch (error) {
      console.error('解析车票数据异常:', error.message);
    }
  }
  
  return tickets;
}

// 获取站点名称
function getStationName(stationMap, stationCode) {
  return stationMap && stationMap[stationCode] ? stationMap[stationCode] : stationCode;
}

// 提取价格信息
function extractPrices(parts) {
  const prices = [];
  const seatTypes = [
    { index: 32, name: '商务座', short: 'swz', code: '9' },
    { index: 31, name: '一等座', short: 'zy', code: 'M' },
    { index: 30, name: '二等座', short: 'ze', code: 'O' },
    { index: 21, name: '高级软卧', short: 'gr', code: '6' },
    { index: 23, name: '软卧', short: 'rw', code: '4' },
    { index: 33, name: '动卧', short: 'srrb', code: 'F' },
    { index: 28, name: '硬卧', short: 'yw', code: '3' },
    { index: 24, name: '软座', short: 'rz', code: '2' },
    { index: 29, name: '硬座', short: 'yz', code: '1' },
    { index: 26, name: '无座', short: 'wz', code: 'W' }
  ];
  
  for (const seatType of seatTypes) {
    if (parts.length > seatType.index && parts[seatType.index] && parts[seatType.index] !== '无') {
      prices.push({
        seatName: seatType.name,
        shortName: seatType.short,
        seatTypeCode: seatType.code,
        num: parts[seatType.index],
        price: null,
        discount: null
      });
    }
  }
  
  return prices;
}

// 提取服务标识
function extractDWFlags(dwFlagStr) {
  const flags = [];
  const dwFlags = ['智能动车组', '复兴号', '静音车厢', '温馨动卧', '动感号', '支持选铺', '老年优惠'];
  
  if (!dwFlagStr) return flags;
  
  const flagsArr = dwFlagStr.split('#');
  for (const flag of flagsArr) {
    for (const dwFlag of dwFlags) {
      if (flag.includes(dwFlag)) {
        flags.push(dwFlag);
        break;
      }
    }
  }
  
  return flags;
}

// 根据车型过滤
function filterTicketsByTrainTypes(tickets, trainFilterFlags) {
  if (!trainFilterFlags) return tickets;
  
  const filters = trainFilterFlags.split('');
  return tickets.filter(ticket => {
    for (const filter of filters) {
      if (matchTrainType(ticket, filter)) {
        return true;
      }
    }
    return false;
  });
}

// 匹配车型
function matchTrainType(ticket, trainType) {
  const trainCode = ticket.startTrainCode;
  
  switch (trainType.toUpperCase()) {
    case 'G':
      return trainCode.startsWith('G') || trainCode.startsWith('C');
    case 'D':
      return trainCode.startsWith('D');
    case 'Z':
      return trainCode.startsWith('Z');
    case 'T':
      return trainCode.startsWith('T');
    case 'K':
      return trainCode.startsWith('K');
    case 'O':
      return !['G', 'D', 'C', 'Z', 'T', 'K'].some(prefix => trainCode.startsWith(prefix));
    case 'F':
      return ticket.dwFlag && ticket.dwFlag.includes('复兴号');
    case 'S':
      return ticket.dwFlag && ticket.dwFlag.includes('智能动车组');
    default:
      return false;
  }
}

// 健康检查
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`12306前端服务已启动，端口: ${PORT}`);
  console.log(`健康检查: http://localhost:${PORT}/health`);
  console.log(`API文档:`);
  console.log(`  GET /api/current-date - 获取当前日期`);
  console.log(`  GET /api/tickets?date=YYYY-MM-DD&fromStation=XXX&toStation=XXX&trainFilterFlags=G - 查询车票`);
});

module.exports = app; 