export const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return '';
  
  let d = date;
  if (typeof date === 'string') {
    d = new Date(date);
  }
  
  // 如果解析失败或日期无效，返回原字符串
  if (!(d instanceof Date) || isNaN(d.getTime())) {
    return date;
  }
  
  const pad = (num, len = 2) => String(num).padStart(len, '0');
  
  const year = d.getFullYear();
  const month = d.getMonth() + 1;
  const day = d.getDate();
  const hours = d.getHours();
  const minutes = d.getMinutes();
  const seconds = d.getSeconds();
  const milliseconds = d.getMilliseconds();
  
  return format
    .replace(/YYYY/g, year)
    .replace(/YY/g, String(year).slice(2))
    .replace(/MM/g, pad(month))
    .replace(/M/g, month)
    .replace(/DD/g, pad(day))
    .replace(/D/g, day)
    .replace(/HH/g, pad(hours))
    .replace(/H/g, hours)
    .replace(/hh/g, pad(hours > 12 ? hours - 12 : hours || 12))
    .replace(/h/g, hours > 12 ? hours - 12 : hours || 12)
    .replace(/mm/g, pad(minutes))
    .replace(/m/g, minutes)
    .replace(/ss/g, pad(seconds))
    .replace(/s/g, seconds)
    .replace(/SSS/g, pad(milliseconds, 3));
};

export const timeAgo = (date) => {
  if (!date) return '';
  
  let d = date;
  if (typeof date === 'string') {
    d = new Date(date);
  }
  
  if (!(d instanceof Date) || isNaN(d.getTime())) {
    return date;
  }
  
  const now = new Date();
  const diff = now.getTime() - d.getTime();
  
  // 定义时间间隔
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  const week = 7 * day;
  const month = 30 * day;
  const year = 365 * day;
  
  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    const minutes = Math.floor(diff / minute);
    return `${minutes}分钟前`;
  } else if (diff < day) {
    const hours = Math.floor(diff / hour);
    return `${hours}小时前`;
  } else if (diff < week) {
    const days = Math.floor(diff / day);
    return `${days}天前`;
  } else if (diff < month) {
    const weeks = Math.floor(diff / week);
    return `${weeks}周前`;
  } else if (diff < year) {
    const months = Math.floor(diff / month);
    return `${months}个月前`;
  } else {
    const years = Math.floor(diff / year);
    return `${years}年前`;
  }
};

export const compareDate = (dateA, dateB) => {
  const a = new Date(dateA);
  const b = new Date(dateB);
  
  // 检查日期是否有效
  if (isNaN(a.getTime()) || isNaN(b.getTime())) {
    return 0;
  }
  
  if (a.getTime() > b.getTime()) return 1;
  if (a.getTime() < b.getTime()) return -1;
  return 0;
}; 