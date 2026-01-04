document.addEventListener('DOMContentLoaded', () => {
    const uploadForm = document.getElementById('upload-form');
    const attendanceFileInput = document.getElementById('attendance-file');
    const rosterListContainer = document.getElementById('roster-list-container');
    const addRosterBtn = document.getElementById('add-roster-btn');

    const loadingSpinner = document.getElementById('loading-spinner');
    const errorAlert = document.getElementById('error-alert');
    const errorMessage = document.getElementById('error-message');
    const successAlert = document.getElementById('success-alert');
    const logCard = document.getElementById('log-card');
    const logContent = document.getElementById('log-content');
    const logStats = document.getElementById('log-stats');
    const downloadContainer = document.getElementById('download-container');

    let rosterCounter = 0;

    const createRosterRow = () => {
        rosterCounter++;
        const rosterRow = document.createElement('div');
        rosterRow.classList.add('roster-row', 'mb-3', 'p-3', 'border', 'rounded');
        rosterRow.setAttribute('data-id', rosterCounter);

        const today = new Date().getDate();

        let dayOptions = '';
        for (let i = 1; i <= 31; i++) {
            dayOptions += `<option value="${i}" ${i === today ? 'selected' : ''}>${i}日</option>`;
        }

        rosterRow.innerHTML = `
            <label class="form-label fw-bold form-text">
                <i class="bi bi-calendar-event"></i> 1.${rosterCounter} 选择值班表及日期
            </label>
            <div class="input-group">
                <input class="form-control duty-roster-file" type="file" accept=".xls,.xlsx" required>
                <select class="form-select day-select" style="max-width: 120px;" required>
                    ${dayOptions}
                </select>
                <button type="button" class="btn btn-danger remove-roster-btn">
                    <i class="bi bi-trash"></i> 移除
                </button>
            </div>
            <div class="form-text">
                <i class="bi bi-info-circle"></i> 请上传用于解析排班信息的 Excel 文件，并选择对应的日期
            </div>
        `;

        // 添加文件选择监听器
        const fileInput = rosterRow.querySelector('.duty-roster-file');
        fileInput.addEventListener('change', (e) => {
            const fileName = e.target.files[0]?.name || '';
            if (fileName) {
                const formText = rosterRow.querySelector('.form-text');
                formText.innerHTML = `<i class="bi bi-check-circle-fill text-success"></i> 已选择: ${fileName}`;
                formText.style.color = '#28a745';
            }
        });

        rosterListContainer.appendChild(rosterRow);

        // 滚动到新添加的行
        rosterRow.scrollIntoView({ behavior: 'smooth', block: 'center' });
    };

    const updateLabels = () => {
        const rows = rosterListContainer.querySelectorAll('.roster-row');
        rows.forEach((row, index) => {
            row.querySelector('label').innerHTML = `<i class="bi bi-calendar-event"></i> 1.${index + 1} 选择值班表及日期`;
        });
    };

    rosterListContainer.addEventListener('click', (e) => {
        if (e.target.classList.contains('remove-roster-btn') || e.target.closest('.remove-roster-btn')) {
            const row = e.target.closest('.roster-row');
            row.style.animation = 'slide-out 0.3s ease-out';
            setTimeout(() => {
                row.remove();
                updateLabels();
            }, 300);
        }
    });

    addRosterBtn.addEventListener('click', () => {
        createRosterRow();
        // 添加按钮点击反馈
        addRosterBtn.style.transform = 'scale(0.95)';
        setTimeout(() => {
            addRosterBtn.style.transform = 'scale(1)';
        }, 150);
    });

    // 监听考勤文件选择
    attendanceFileInput.addEventListener('change', (e) => {
        const fileName = e.target.files[0]?.name || '';
        const helpText = document.getElementById('attendanceHelp');
        if (fileName) {
            helpText.innerHTML = `<i class="bi bi-check-circle-fill text-success"></i> 已选择: ${fileName}`;
            helpText.style.color = '#28a745';
        }
    });

    uploadForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const rosterRows = rosterListContainer.querySelectorAll('.roster-row');
        const attendanceFile = attendanceFileInput.files[0];

        if (rosterRows.length === 0) {
            showNotification('请至少添加一个值班表！', 'warning');
            return;
        }
        if (!attendanceFile) {
            showNotification('请确保已选择考勤记录表！', 'warning');
            return;
        }

        // 清理旧结果和错误
        logCard.classList.add('d-none');
        logContent.textContent = '';
        logStats.innerHTML = '';
        downloadContainer.innerHTML = '';
        errorAlert.classList.add('d-none');
        successAlert.classList.add('d-none');
        loadingSpinner.classList.remove('d-none');

        const formData = new FormData();
        formData.append('attendanceFile', attendanceFile);

        let filesValid = true;
        rosterRows.forEach((row, index) => {
            const fileInput = row.querySelector('.duty-roster-file');
            const daySelect = row.querySelector('.day-select');

            if (fileInput.files.length === 0) {
                filesValid = false;
            } else {
                formData.append('dutyRosterFiles', fileInput.files[0]);
                formData.append('days', daySelect.value);
            }
        });

        if (!filesValid) {
            showNotification('请确保每一个值班表都已选择文件。', 'warning');
            loadingSpinner.classList.add('d-none');
            return;
        }

        try {
            const response = await fetch('process-roster', {
                method: 'POST',
                body: formData,
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `服务器错误: ${response.statusText}`);
            }

            const result = await response.json();

            // 显示成功消息
            successAlert.classList.remove('d-none');

            // 分析日志并显示统计信息
            const logs = result.logs;
            const stats = analyzeLogs(logs);
            displayStats(stats);

            // 显示日志
            logContent.textContent = logs.join('\n');
            logCard.classList.remove('d-none');

            // 创建下载按钮
            const downloadBtn = document.createElement('a');
            downloadBtn.href = `download/${result.transactionId}`;
            downloadBtn.className = 'btn btn-success btn-lg';
            downloadBtn.innerHTML = '<i class="bi bi-download"></i> 下载更新后的考勤表';
            downloadBtn.setAttribute('role', 'button');
            downloadContainer.appendChild(downloadBtn);

            // 滚动到结果区域
            logCard.scrollIntoView({ behavior: 'smooth', block: 'start' });

        } catch (error) {
            console.error('处理失败:', error);
            errorMessage.textContent = error.message;
            errorAlert.classList.remove('d-none');
            errorAlert.scrollIntoView({ behavior: 'smooth', block: 'center' });
        } finally {
            loadingSpinner.classList.add('d-none');
        }
    });

    // 分析日志并提取统计信息
    const analyzeLogs = (logs) => {
        const stats = {
            totalFiles: 0,
            totalUpdates: 0,
            processedDays: new Set(),
            uniqueEmployees: new Set(),
        };

        logs.forEach(log => {
            // 统计处理的文件数量
            if (log.includes('正在处理:')) {
                stats.totalFiles++;
            }
            // 统计更新的数量
            if (log.includes('[收集] 更新')) {
                stats.totalUpdates++;
            }
            // 提取日期
            const dayMatch = log.match(/(\d+)日/);
            if (dayMatch) {
                stats.processedDays.add(parseInt(dayMatch[1]));
            }
            // 提取员工姓名（简单匹配）
            const nameMatch = log.match(/更新\s+(\u4e00-\u9fa5+)\s+/);
            if (nameMatch) {
                stats.uniqueEmployees.add(nameMatch[1]);
            }
        });

        return {
            totalFiles: stats.totalFiles,
            totalUpdates: stats.totalUpdates,
            processedDays: stats.processedDays.size,
            uniqueEmployees: stats.uniqueEmployees.size,
        };
    };

    // 显示统计信息
    const displayStats = (stats) => {
        const statsHtml = `
            <div class="stat-item">
                <i class="bi bi-file-earmark-spreadsheet"></i>
                <span>处理文件: ${stats.totalFiles} 个</span>
            </div>
            <div class="stat-item">
                <i class="bi bi-pencil-square"></i>
                <span>更新记录: ${stats.totalUpdates} 条</span>
            </div>
            <div class="stat-item">
                <i class="bi bi-calendar-range"></i>
                <span>处理日期: ${stats.processedDays} 天</span>
            </div>
            ${stats.uniqueEmployees > 0 ? `
            <div class="stat-item">
                <i class="bi bi-people"></i>
                <span>涉及员工: ${stats.uniqueEmployees} 人</span>
            </div>
            ` : ''}
        `;
        logStats.innerHTML = statsHtml;
    };

    // 显示通知消息
    const showNotification = (message, type = 'info') => {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type}`;
        notification.style.position = 'fixed';
        notification.style.top = '20px';
        notification.style.right = '20px';
        notification.style.zIndex = '9999';
        notification.style.minWidth = '300px';
        notification.style.animation = 'slide-in 0.3s ease-out';

        const icon = type === 'warning' ? 'bi-exclamation-triangle-fill' :
                     type === 'success' ? 'bi-check-circle-fill' :
                     'bi-info-circle-fill';

        notification.innerHTML = `<i class="bi ${icon}"></i> ${message}`;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.style.animation = 'slide-out 0.3s ease-out';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    };

    // Initially add one roster row for the user
    createRosterRow();
});
